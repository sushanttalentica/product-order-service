package com.ecommerce.productorder.payment.service.impl;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import com.ecommerce.productorder.payment.service.PaymentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisherImpl implements PaymentEventPublisher {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // Kafka topic names
    private static final String PAYMENT_PROCESSED_TOPIC = "payment.processed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    private static final String PAYMENT_REFUNDED_TOPIC = "payment.refunded";
    private static final String PAYMENT_CANCELLED_TOPIC = "payment.cancelled";
    private static final String PAYMENT_RETRY_TOPIC = "payment.retry";
    

    @Override
    public void publishPaymentProcessedEvent(Payment payment) {
        log.info("Publishing payment processed event for payment ID: {}", payment.getPaymentId());
        
        try {
            Map<String, Object> eventData = createPaymentEventData(payment, "PAYMENT_PROCESSED");
            kafkaTemplate.send(PAYMENT_PROCESSED_TOPIC, payment.getPaymentId(), eventData);
            
            log.info("Payment processed event published successfully for payment ID: {}", payment.getPaymentId());
        } catch (Exception e) {
            log.error("Error publishing payment processed event for payment ID: {}", payment.getPaymentId(), e);
            // Graceful degradation - don't throw exception to avoid breaking payment flow
        }
    }
    

    @Override
    public void publishPaymentFailedEvent(Payment payment) {
        log.info("Publishing payment failed event for payment ID: {}", payment.getPaymentId());
        
        try {
            Map<String, Object> eventData = createPaymentEventData(payment, "PAYMENT_FAILED");
            eventData.put("failureReason", payment.getFailureReason());
            kafkaTemplate.send(PAYMENT_FAILED_TOPIC, payment.getPaymentId(), eventData);
            
            log.info("Payment failed event published successfully for payment ID: {}", payment.getPaymentId());
        } catch (Exception e) {
            log.error("Error publishing payment failed event for payment ID: {}", payment.getPaymentId(), e);
            // Graceful degradation - don't throw exception to avoid breaking payment flow
        }
    }
    

    @Override
    public void publishPaymentRefundedEvent(Payment payment) {
        log.info("Publishing payment refunded event for payment ID: {}", payment.getPaymentId());
        
        try {
            Map<String, Object> eventData = createPaymentEventData(payment, "PAYMENT_REFUNDED");
            kafkaTemplate.send(PAYMENT_REFUNDED_TOPIC, payment.getPaymentId(), eventData);
            
            log.info("Payment refunded event published successfully for payment ID: {}", payment.getPaymentId());
        } catch (Exception e) {
            log.error("Error publishing payment refunded event for payment ID: {}", payment.getPaymentId(), e);
            // Graceful degradation - don't throw exception to avoid breaking payment flow
        }
    }
    

    @Override
    public void publishPaymentCancelledEvent(Payment payment) {
        log.info("Publishing payment cancelled event for payment ID: {}", payment.getPaymentId());
        
        try {
            Map<String, Object> eventData = createPaymentEventData(payment, "PAYMENT_CANCELLED");
            kafkaTemplate.send(PAYMENT_CANCELLED_TOPIC, payment.getPaymentId(), eventData);
            
            log.info("Payment cancelled event published successfully for payment ID: {}", payment.getPaymentId());
        } catch (Exception e) {
            log.error("Error publishing payment cancelled event for payment ID: {}", payment.getPaymentId(), e);
            // Graceful degradation - don't throw exception to avoid breaking payment flow
        }
    }
    

    @Override
    public void publishPaymentRetryEvent(Payment payment) {
        log.info("Publishing payment retry event for payment ID: {}", payment.getPaymentId());
        
        try {
            Map<String, Object> eventData = createPaymentEventData(payment, "PAYMENT_RETRY");
            kafkaTemplate.send(PAYMENT_RETRY_TOPIC, payment.getPaymentId(), eventData);
            
            log.info("Payment retry event published successfully for payment ID: {}", payment.getPaymentId());
        } catch (Exception e) {
            log.error("Error publishing payment retry event for payment ID: {}", payment.getPaymentId(), e);
            // Graceful degradation - don't throw exception to avoid breaking payment flow
        }
    }
    

    private Map<String, Object> createPaymentEventData(Payment payment, String eventType) {
        Map<String, Object> eventData = new HashMap<>();
        
        // Basic payment information
        eventData.put("eventType", eventType);
        eventData.put("eventId", java.util.UUID.randomUUID().toString());
        eventData.put("timestamp", LocalDateTime.now().toString());
        eventData.put("paymentId", payment.getPaymentId());
        eventData.put("orderId", payment.getOrderId());
        eventData.put("customerId", payment.getCustomerId());
        eventData.put("amount", payment.getAmount());
        eventData.put("status", payment.getStatus().name());
        eventData.put("paymentMethod", payment.getPaymentMethod().name());
        eventData.put("transactionId", payment.getTransactionId());
        eventData.put("gatewayResponse", payment.getGatewayResponse());
        eventData.put("processedAt", payment.getProcessedAt());
        eventData.put("createdAt", payment.getCreatedAt());
        eventData.put("updatedAt", payment.getUpdatedAt());
        
        // Service information
        eventData.put("serviceName", "product-order-service");
        eventData.put("serviceVersion", "1.0.0");
        
        return eventData;
    }
}
