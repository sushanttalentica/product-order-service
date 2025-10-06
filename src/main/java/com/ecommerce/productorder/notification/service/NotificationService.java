package com.ecommerce.productorder.notification.service;

import com.ecommerce.productorder.domain.entity.Order;

import java.util.Optional;

/**
 * Service interface for Notification operations
 * 
 * Design Principles Applied:
 * - Interface Segregation: Defines only necessary notification operations
 * - Single Responsibility: Only handles notification business logic
 * - Dependency Inversion: Depends on abstractions, not implementations
 * - Optional Return Types: Uses Optional for null-safe operations
 * - Business Logic Encapsulation: Encapsulates notification business rules
 */
public interface NotificationService {
    
    /**
     * Sends order confirmation email
     * Sends email notification for order confirmation
     * 
     * @param order the order entity
     * @return true if email sent successfully, false otherwise
     */
    boolean sendOrderConfirmationEmail(Order order);
    
    /**
     * Sends order status update email
     * Sends email notification for order status updates
     * 
     * @param order the order entity
     * @param previousStatus the previous order status
     * @return true if email sent successfully, false otherwise
     */
    boolean sendOrderStatusUpdateEmail(Order order, String previousStatus);
    
    /**
     * Sends order cancellation email
     * Sends email notification for order cancellation
     * 
     * @param order the order entity
     * @return true if email sent successfully, false otherwise
     */
    boolean sendOrderCancellationEmail(Order order);
    
    /**
     * Sends payment confirmation email
     * Sends email notification for payment confirmation
     * 
     * @param order the order entity
     * @param paymentId the payment ID
     * @return true if email sent successfully, false otherwise
     */
    boolean sendPaymentConfirmationEmail(Order order, String paymentId);
    
    /**
     * Sends invoice email
     * Sends email notification with invoice attachment
     * 
     * @param order the order entity
     * @param invoiceUrl the invoice URL
     * @return true if email sent successfully, false otherwise
     */
    boolean sendInvoiceEmail(Order order, String invoiceUrl);
    
    /**
     * Sends low stock alert email
     * Sends email notification for low stock alerts
     * 
     * @param productName the product name
     * @param currentStock the current stock level
     * @param threshold the stock threshold
     * @return true if email sent successfully, false otherwise
     */
    boolean sendLowStockAlertEmail(String productName, Integer currentStock, Integer threshold);
    
    /**
     * Gets notification status
     * Retrieves notification delivery status
     * 
     * @param notificationId the notification ID
     * @return Optional containing notification status if found, empty otherwise
     */
    Optional<String> getNotificationStatus(String notificationId);
}
