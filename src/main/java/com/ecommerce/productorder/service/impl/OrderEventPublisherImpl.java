package com.ecommerce.productorder.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.service.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of OrderEventPublisher
 * Publishes order events to Kafka topics
 * 
 * Design Principles Applied:
 * - Service Layer Pattern: Encapsulates event publishing logic
 * - Single Responsibility: Only handles order event publishing
 * - Dependency Injection: Uses constructor injection for dependencies
 * - Observer Pattern: Publishes events for other services to observe
 * - Event-Driven Architecture: Enables loose coupling between services
 * - Logging: Uses SLF4J for comprehensive logging
 * - Exception Handling: Proper exception handling with graceful degradation
 * - Factory Pattern: Uses static factory methods for event creation
 * - Builder Pattern: Uses Builder pattern for event data creation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisherImpl implements OrderEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // Kafka topic names
    private static final String ORDER_CREATED_TOPIC = "order.created";
    private static final String ORDER_STATUS_UPDATED_TOPIC = "order.status.updated";
    private static final String ORDER_CANCELLED_TOPIC = "order.cancelled";
    private static final String ORDER_COMPLETED_TOPIC = "order.completed";
    
    /**
     * Publishes order created event
     * Notifies other services about new order creation
     * 
     * @param order the order entity
     */
    @Override
    public void publishOrderCreatedEvent(Order order) {
        log.info("Publishing order created event for order ID: {}", order.getId());
        
        try {
            Map<String, Object> eventData = createOrderEventData(order, "ORDER_CREATED");
            kafkaTemplate.send(ORDER_CREATED_TOPIC, order.getId().toString(), eventData);
            
            log.info("Order created event published successfully for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing order created event for order ID: {}", order.getId(), e);
            // Graceful degradation - don't throw exception to avoid breaking order flow
        }
    }
    
    /**
     * Publishes order status updated event
     * Notifies other services about order status changes
     * 
     * @param order the order entity
     */
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
            // Graceful degradation - don't throw exception to avoid breaking order flow
        }
    }
    
    /**
     * Publishes order cancelled event
     * Notifies other services about order cancellation
     * 
     * @param order the order entity
     */
    @Override
    public void publishOrderCancelledEvent(Order order) {
        log.info("Publishing order cancelled event for order ID: {}", order.getId());
        
        try {
            Map<String, Object> eventData = createOrderEventData(order, "ORDER_CANCELLED");
            kafkaTemplate.send(ORDER_CANCELLED_TOPIC, order.getId().toString(), eventData);
            
            log.info("Order cancelled event published successfully for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing order cancelled event for order ID: {}", order.getId(), e);
            // Graceful degradation - don't throw exception to avoid breaking order flow
        }
    }
    
    /**
     * Publishes order completed event
     * Notifies other services about order completion
     * 
     * @param order the order entity
     */
    @Override
    public void publishOrderCompletedEvent(Order order) {
        log.info("Publishing order completed event for order ID: {}", order.getId());
        
        try {
            Map<String, Object> eventData = createOrderEventData(order, "ORDER_COMPLETED");
            kafkaTemplate.send(ORDER_COMPLETED_TOPIC, order.getId().toString(), eventData);
            
            log.info("Order completed event published successfully for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing order completed event for order ID: {}", order.getId(), e);
            // Graceful degradation - don't throw exception to avoid breaking order flow
        }
    }
    
    /**
     * Creates order event data
     * Factory method for creating event data
     * 
     * @param order the order entity
     * @param eventType the type of event
     * @return Map containing event data
     */
    private Map<String, Object> createOrderEventData(Order order, String eventType) {
        Map<String, Object> eventData = new HashMap<>();
        
        // Basic order information
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
