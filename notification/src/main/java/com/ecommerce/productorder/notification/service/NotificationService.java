package com.ecommerce.productorder.notification.service;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.notification.model.NotificationChannel;
import com.ecommerce.productorder.notification.model.NotificationRequest;
import java.util.Optional;
import java.util.Set;

public interface NotificationService {

  boolean sendNotification(NotificationRequest request);

  boolean notifyOrderConfirmation(Order order, Set<NotificationChannel> channels);

  boolean notifyOrderStatusUpdate(
      Order order, String previousStatus, Set<NotificationChannel> channels);

  boolean notifyOrderCancellation(Order order, Set<NotificationChannel> channels);

  boolean notifyPaymentConfirmation(
      Order order, String paymentId, Set<NotificationChannel> channels);

  boolean notifyInvoice(Order order, String invoiceUrl, Set<NotificationChannel> channels);

  boolean notifyLowStockAlert(
      String productName,
      Integer currentStock,
      Integer threshold,
      Set<NotificationChannel> channels);

  Optional<String> getNotificationStatus(String notificationId);
}
