package com.ecommerce.productorder.notification.service;

import com.ecommerce.productorder.domain.entity.Order;

import java.util.Optional;

public interface NotificationService {
    

    boolean sendOrderConfirmationEmail(Order order);
    

    boolean sendOrderStatusUpdateEmail(Order order, String previousStatus);
    

    boolean sendOrderCancellationEmail(Order order);
    

    boolean sendPaymentConfirmationEmail(Order order, String paymentId);
    

    boolean sendInvoiceEmail(Order order, String invoiceUrl);
    

    boolean sendLowStockAlertEmail(String productName, Integer currentStock, Integer threshold);
    

    Optional<String> getNotificationStatus(String notificationId);
}
