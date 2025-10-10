package com.ecommerce.productorder.notification.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // Kafka topics for notifications
    private static final String NOTIFICATION_EMAIL_TOPIC = "notification.email";
    private static final String NOTIFICATION_SMS_TOPIC = "notification.sms";
    private static final String NOTIFICATION_PUSH_TOPIC = "notification.push";
    

    @Override
    public boolean sendOrderConfirmationEmail(Order order) {
        log.info("Sending order confirmation email for order ID: {}", order.getId());
        
        try {
            // Create email notification
            Map<String, Object> emailData = createOrderConfirmationEmailData(order);
            
            // Send to notification service via Kafka
            kafkaTemplate.send(NOTIFICATION_EMAIL_TOPIC, order.getId().toString(), emailData);
            
            log.info("Order confirmation email sent successfully for order ID: {}", order.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error sending order confirmation email for order ID: {}", order.getId(), e);
            return false;
        }
    }
    

    @Override
    public boolean sendOrderStatusUpdateEmail(Order order, String previousStatus) {
        log.info("Sending order status update email for order ID: {}", order.getId());
        
        try {
            // Create email notification
            Map<String, Object> emailData = createOrderStatusUpdateEmailData(order, previousStatus);
            
            // Send to notification service via Kafka
            kafkaTemplate.send(NOTIFICATION_EMAIL_TOPIC, order.getId().toString(), emailData);
            
            log.info("Order status update email sent successfully for order ID: {}", order.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error sending order status update email for order ID: {}", order.getId(), e);
            return false;
        }
    }
    

    @Override
    public boolean sendOrderCancellationEmail(Order order) {
        log.info("Sending order cancellation email for order ID: {}", order.getId());
        
        try {
            // Create email notification
            Map<String, Object> emailData = createOrderCancellationEmailData(order);
            
            // Send to notification service via Kafka
            kafkaTemplate.send(NOTIFICATION_EMAIL_TOPIC, order.getId().toString(), emailData);
            
            log.info("Order cancellation email sent successfully for order ID: {}", order.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error sending order cancellation email for order ID: {}", order.getId(), e);
            return false;
        }
    }
    

    @Override
    public boolean sendPaymentConfirmationEmail(Order order, String paymentId) {
        log.info("Sending payment confirmation email for order ID: {}", order.getId());
        
        try {
            // Create email notification
            Map<String, Object> emailData = createPaymentConfirmationEmailData(order, paymentId);
            
            // Send to notification service via Kafka
            kafkaTemplate.send(NOTIFICATION_EMAIL_TOPIC, order.getId().toString(), emailData);
            
            log.info("Payment confirmation email sent successfully for order ID: {}", order.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error sending payment confirmation email for order ID: {}", order.getId(), e);
            return false;
        }
    }
    

    @Override
    public boolean sendInvoiceEmail(Order order, String invoiceUrl) {
        log.info("Sending invoice email for order ID: {}", order.getId());
        
        try {
            // Create email notification
            Map<String, Object> emailData = createInvoiceEmailData(order, invoiceUrl);
            
            // Send to notification service via Kafka
            kafkaTemplate.send(NOTIFICATION_EMAIL_TOPIC, order.getId().toString(), emailData);
            
            log.info("Invoice email sent successfully for order ID: {}", order.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error sending invoice email for order ID: {}", order.getId(), e);
            return false;
        }
    }
    

    @Override
    public boolean sendLowStockAlertEmail(String productName, Integer currentStock, Integer threshold) {
        log.info("Sending low stock alert email for product: {}", productName);
        
        try {
            // Create email notification
            Map<String, Object> emailData = createLowStockAlertEmailData(productName, currentStock, threshold);
            
            // Send to notification service via Kafka
            kafkaTemplate.send(NOTIFICATION_EMAIL_TOPIC, "low-stock-alert", emailData);
            
            log.info("Low stock alert email sent successfully for product: {}", productName);
            return true;
            
        } catch (Exception e) {
            log.error("Error sending low stock alert email for product: {}", productName, e);
            return false;
        }
    }
    

    @Override
    public Optional<String> getNotificationStatus(String notificationId) {
        log.debug("Getting notification status for ID: {}", notificationId);
        
        try {
            // This would typically query a notification service or database
            // For now, return a mock status
            return Optional.of("DELIVERED");
            
        } catch (Exception e) {
            log.error("Error getting notification status for ID: {}", notificationId, e);
            return Optional.empty();
        }
    }
    

    @KafkaListener(topics = "order.created", groupId = "notification-service")
    public void handleOrderCreatedEvent(Map<String, Object> eventData) {
        log.info("Received order created event: {}", eventData);
        
        try {
            // Extract order information from event
            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            String customerEmail = eventData.get("customerEmail").toString();
            
            // Create mock order for notification
            Order order = createMockOrder(orderId, customerEmail);
            
            // Send confirmation email
            sendOrderConfirmationEmail(order);
            
        } catch (Exception e) {
            log.error("Error handling order created event", e);
        }
    }
    

    @KafkaListener(topics = "payment.processed", groupId = "notification-service")
    public void handlePaymentProcessedEvent(Map<String, Object> eventData) {
        log.info("Received payment processed event: {}", eventData);
        
        try {
            // Extract payment information from event
            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            String paymentId = eventData.get("paymentId").toString();
            String customerEmail = eventData.get("customerEmail").toString();
            
            // Create mock order for notification
            Order order = createMockOrder(orderId, customerEmail);
            
            // Send payment confirmation email
            sendPaymentConfirmationEmail(order, paymentId);
            
        } catch (Exception e) {
            log.error("Error handling payment processed event", e);
        }
    }
    

    private Map<String, Object> createOrderConfirmationEmailData(Order order) {
        Map<String, Object> emailData = new HashMap<>();
        
        emailData.put("notificationType", "ORDER_CONFIRMATION");
        emailData.put("notificationId", UUID.randomUUID().toString());
        emailData.put("timestamp", LocalDateTime.now().toString());
        emailData.put("recipientEmail", order.getCustomerEmail());
        emailData.put("subject", "Order Confirmation - " + order.getOrderNumber());
        emailData.put("template", "order-confirmation");
        emailData.put("data", Map.of(
            "orderId", order.getId(),
            "orderNumber", order.getOrderNumber(),
            "customerEmail", order.getCustomerEmail(),
            "totalAmount", order.getTotalAmount(),
            "status", order.getStatus().name()
        ));
        
        return emailData;
    }
    

    private Map<String, Object> createOrderStatusUpdateEmailData(Order order, String previousStatus) {
        Map<String, Object> emailData = new HashMap<>();
        
        emailData.put("notificationType", "ORDER_STATUS_UPDATE");
        emailData.put("notificationId", UUID.randomUUID().toString());
        emailData.put("timestamp", LocalDateTime.now().toString());
        emailData.put("recipientEmail", order.getCustomerEmail());
        emailData.put("subject", "Order Status Update - " + order.getOrderNumber());
        emailData.put("template", "order-status-update");
        emailData.put("data", Map.of(
            "orderId", order.getId(),
            "orderNumber", order.getOrderNumber(),
            "customerEmail", order.getCustomerEmail(),
            "previousStatus", previousStatus,
            "currentStatus", order.getStatus().name(),
            "totalAmount", order.getTotalAmount()
        ));
        
        return emailData;
    }
    

    private Map<String, Object> createOrderCancellationEmailData(Order order) {
        Map<String, Object> emailData = new HashMap<>();
        
        emailData.put("notificationType", "ORDER_CANCELLATION");
        emailData.put("notificationId", UUID.randomUUID().toString());
        emailData.put("timestamp", LocalDateTime.now().toString());
        emailData.put("recipientEmail", order.getCustomerEmail());
        emailData.put("subject", "Order Cancelled - " + order.getOrderNumber());
        emailData.put("template", "order-cancellation");
        emailData.put("data", Map.of(
            "orderId", order.getId(),
            "orderNumber", order.getOrderNumber(),
            "customerEmail", order.getCustomerEmail(),
            "totalAmount", order.getTotalAmount()
        ));
        
        return emailData;
    }
    

    private Map<String, Object> createPaymentConfirmationEmailData(Order order, String paymentId) {
        Map<String, Object> emailData = new HashMap<>();
        
        emailData.put("notificationType", "PAYMENT_CONFIRMATION");
        emailData.put("notificationId", UUID.randomUUID().toString());
        emailData.put("timestamp", LocalDateTime.now().toString());
        emailData.put("recipientEmail", order.getCustomerEmail());
        emailData.put("subject", "Payment Confirmed - " + order.getOrderNumber());
        emailData.put("template", "payment-confirmation");
        emailData.put("data", Map.of(
            "orderId", order.getId(),
            "orderNumber", order.getOrderNumber(),
            "paymentId", paymentId,
            "customerEmail", order.getCustomerEmail(),
            "totalAmount", order.getTotalAmount()
        ));
        
        return emailData;
    }
    

    private Map<String, Object> createInvoiceEmailData(Order order, String invoiceUrl) {
        Map<String, Object> emailData = new HashMap<>();
        
        emailData.put("notificationType", "INVOICE");
        emailData.put("notificationId", UUID.randomUUID().toString());
        emailData.put("timestamp", LocalDateTime.now().toString());
        emailData.put("recipientEmail", order.getCustomerEmail());
        emailData.put("subject", "Invoice - " + order.getOrderNumber());
        emailData.put("template", "invoice");
        emailData.put("data", Map.of(
            "orderId", order.getId(),
            "orderNumber", order.getOrderNumber(),
            "customerEmail", order.getCustomerEmail(),
            "totalAmount", order.getTotalAmount(),
            "invoiceUrl", invoiceUrl
        ));
        
        return emailData;
    }
    

    private Map<String, Object> createLowStockAlertEmailData(String productName, Integer currentStock, Integer threshold) {
        Map<String, Object> emailData = new HashMap<>();
        
        emailData.put("notificationType", "LOW_STOCK_ALERT");
        emailData.put("notificationId", UUID.randomUUID().toString());
        emailData.put("timestamp", LocalDateTime.now().toString());
        emailData.put("recipientEmail", "admin@ecommerce.com");
        emailData.put("subject", "Low Stock Alert - " + productName);
        emailData.put("template", "low-stock-alert");
        emailData.put("data", Map.of(
            "productName", productName,
            "currentStock", currentStock,
            "threshold", threshold
        ));
        
        return emailData;
    }
    

    private Order createMockOrder(Long orderId, String customerEmail) {
        return Order.builder()
                .id(orderId)
                .orderNumber("ORD-" + orderId)
                .customerId(1L)
                .customerEmail(customerEmail)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(java.math.BigDecimal.valueOf(100.00))
                .build();
    }
}
