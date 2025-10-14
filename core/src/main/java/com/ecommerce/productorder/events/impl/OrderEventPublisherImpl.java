package com.ecommerce.productorder.events.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.events.OrderEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class OrderEventPublisherImpl implements OrderEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;    
    public OrderEventPublisherImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    // Kafka topics
    private static final String ORDER_CREATED_TOPIC = "order.created";
    private static final String ORDER_STATUS_UPDATED_TOPIC = "order.status.updated";
    private static final String ORDER_CANCELLED_TOPIC = "order.cancelled";
    private static final String ORDER_COMPLETED_TOPIC = "order.completed";
    

    @Override
    public void publishOrderCreatedEvent(Order order) {
        log.info("Publishing order created event for order ID: {}", order.getId());
        
        try {
            Map<String, Object> eventData = createOrderEventData(order, "ORDER_CREATED");
            kafkaTemplate.send(ORDER_CREATED_TOPIC, order.getId().toString(), eventData);
            
            log.info("Order created event published successfully for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing order created event for order ID: {}", order.getId(), e);
        }
    }
    

    @Override
    public void publishOrderStatusUpdatedEvent(Order order) {
        log.info("Publishing order status updated event for order ID: {}", order.getId());
        
        try {
            Map<String, Object> eventData = createOrderEventData(order, "ORDER_STATUS_UPDATED");
            eventData.put("previousStatus", "PENDING"); // This would typically be tracked
            kafkaTemplate.send(ORDER_STATUS_UPDATED_TOPIC, order.getId().toString(), eventData);
            
            log.info("Order status updated event published successfully for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing order status updated event for order ID: {}", order.getId(), e);
        }
    }
    

    @Override
    public void publishOrderCancelledEvent(Order order) {
        log.info("Publishing order cancelled event for order ID: {}", order.getId());
        
        try {
            Map<String, Object> eventData = createOrderEventData(order, "ORDER_CANCELLED");
            kafkaTemplate.send(ORDER_CANCELLED_TOPIC, order.getId().toString(), eventData);
            
            log.info("Order cancelled event published successfully for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing order cancelled event for order ID: {}", order.getId(), e);
        }
    }
    

    @Override
    public void publishOrderCompletedEvent(Order order) {
        log.info("Publishing order completed event for order ID: {}", order.getId());
        
        try {
            Map<String, Object> eventData = createOrderEventData(order, "ORDER_COMPLETED");
            kafkaTemplate.send(ORDER_COMPLETED_TOPIC, order.getId().toString(), eventData);
            
            log.info("Order completed event published successfully for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing order completed event for order ID: {}", order.getId(), e);
        }
    }
    

    private Map<String, Object> createOrderEventData(Order order, String eventType) {
        Map<String, Object> eventData = new HashMap<>();
        
        // Order information
        eventData.put("eventType", eventType);
        eventData.put("eventId", java.util.UUID.randomUUID().toString());
        eventData.put("timestamp", LocalDateTime.now().toString());
        eventData.put("orderId", order.getId());
        eventData.put("orderNumber", order.getOrderNumber());
        eventData.put("customerId", order.getCustomerId());
        eventData.put("customerEmail", order.getCustomerEmail());
        eventData.put("status", order.getStatus().name());
        eventData.put("totalAmount", order.getTotalAmount());
        eventData.put("shippingAddress", order.getShippingAddress());
        eventData.put("createdAt", order.getCreatedAt());
        eventData.put("updatedAt", order.getUpdatedAt());
        
        // Service information
        eventData.put("serviceName", "product-order-service");
        eventData.put("serviceVersion", "1.0.0");
        
        return eventData;
    }
}
