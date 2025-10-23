package com.ecommerce.productorder.notification.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.repository.OrderRepository;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import com.ecommerce.productorder.notification.model.NotificationChannel;
import com.ecommerce.productorder.notification.model.NotificationRequest;
import com.ecommerce.productorder.notification.model.NotificationStatusPattern;
import com.ecommerce.productorder.notification.model.NotificationType;
import com.ecommerce.productorder.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaBackedNotificationService implements NotificationService {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final OrderRepository orderRepository;

  public KafkaBackedNotificationService(
      KafkaTemplate<String, Object> kafkaTemplate, OrderRepository orderRepository) {
    this.kafkaTemplate = kafkaTemplate;
    this.orderRepository = orderRepository;
  }

  private static final Map<NotificationChannel, String> CHANNEL_TOPICS =
      Map.of(
          NotificationChannel.EMAIL, "notification.email",
          NotificationChannel.SMS, "notification.sms",
          NotificationChannel.PUSH, "notification.push",
          NotificationChannel.WEBHOOK, "notification.webhook");

  @Override
  public boolean sendNotification(NotificationRequest request) {
    return request.getChannels().stream()
        .map(channel -> sendToChannel(request, channel))
        .reduce(true, Boolean::logicalAnd);
  }

  private boolean sendToChannel(NotificationRequest request, NotificationChannel channel) {
    try {
      kafkaTemplate.send(
          CHANNEL_TOPICS.get(channel),
          request.getRecipient(),
          buildNotificationData(request, channel));
      return true;
    } catch (Exception e) {
      log.error("Notification failed: {} via {}", request.getType(), channel);
      return false;
    }
  }

  @Override
  public boolean notifyOrderConfirmation(Order order, Set<NotificationChannel> channels) {
    // Build notification with order details and send via specified channels
    return sendNotification(
        buildRequest(
            NotificationType.ORDER_CONFIRMATION,
            channels,
            order.getCustomerEmail(),
            NotificationStatusPattern.ORDER_CONFIRMATION.format(order.getOrderNumber()),
            Map.of(
                "orderId",
                order.getId(),
                "orderNumber",
                order.getOrderNumber(),
                "customerEmail",
                order.getCustomerEmail(),
                "totalAmount",
                order.getTotalAmount(),
                "status",
                order.getStatus().name())));
  }

  @Override
  public boolean notifyOrderStatusUpdate(
      Order order, String previousStatus, Set<NotificationChannel> channels) {
    // Notify customer of order status transition
    return sendNotification(
        buildRequest(
            NotificationType.ORDER_STATUS_UPDATE,
            channels,
            order.getCustomerEmail(),
            NotificationStatusPattern.ORDER_STATUS_UPDATE.format(order.getOrderNumber()),
            Map.of(
                "orderId",
                order.getId(),
                "orderNumber",
                order.getOrderNumber(),
                "previousStatus",
                previousStatus,
                "currentStatus",
                order.getStatus().name())));
  }

  @Override
  public boolean notifyOrderCancellation(Order order, Set<NotificationChannel> channels) {
    // Notify customer of order cancellation
    return sendNotification(
        buildRequest(
            NotificationType.ORDER_CANCELLATION,
            channels,
            order.getCustomerEmail(),
            NotificationStatusPattern.ORDER_CANCELLATION.format(order.getOrderNumber()),
            Map.of(
                "orderId",
                order.getId(),
                "orderNumber",
                order.getOrderNumber(),
                "totalAmount",
                order.getTotalAmount())));
  }

  @Override
  public boolean notifyPaymentConfirmation(
      Order order, String paymentId, Set<NotificationChannel> channels) {
    // Notify customer of successful payment processing
    return sendNotification(
        buildRequest(
            NotificationType.PAYMENT_CONFIRMATION,
            channels,
            order.getCustomerEmail(),
            NotificationStatusPattern.PAYMENT_CONFIRMATION.format(order.getOrderNumber()),
            Map.of(
                "orderId",
                order.getId(),
                "orderNumber",
                order.getOrderNumber(),
                "paymentId",
                paymentId,
                "totalAmount",
                order.getTotalAmount())));
  }

  @Override
  public boolean notifyInvoice(Order order, String invoiceUrl, Set<NotificationChannel> channels) {
    // Send invoice document link to customer
    return sendNotification(
        buildRequest(
            NotificationType.INVOICE,
            channels,
            order.getCustomerEmail(),
            NotificationStatusPattern.INVOICE.format(order.getOrderNumber()),
            Map.of(
                "orderId",
                order.getId(),
                "orderNumber",
                order.getOrderNumber(),
                "totalAmount",
                order.getTotalAmount(),
                "invoiceUrl",
                invoiceUrl)));
  }

  @Override
  public boolean notifyLowStockAlert(
      String productName,
      Integer currentStock,
      Integer threshold,
      Set<NotificationChannel> channels) {
    // Alert admin when product stock falls below threshold
    return sendNotification(
        buildRequest(
            NotificationType.LOW_STOCK_ALERT,
            channels,
            "admin@ecommerce.com",
            NotificationStatusPattern.LOW_STOCK_ALERT.format(productName),
            Map.of(
                "productName", productName, "currentStock", currentStock, "threshold", threshold)));
  }

  @Override
  public Optional<String> getNotificationStatus(String notificationId) {
    return Optional.of("DELIVERED");
  }

  @KafkaListener(topics = "order.created", groupId = "notification-service")
  public void handleOrderCreatedEvent(String eventDataJson) {
    try {
      // Parse JSON string to Map
      com.fasterxml.jackson.databind.ObjectMapper mapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      Map<String, Object> eventData = mapper.readValue(eventDataJson, Map.class);

      Long orderId = Long.valueOf(eventData.get("orderId").toString());
      Order order =
          orderRepository
              .findById(orderId)
              .orElseThrow(
                  () -> new ResourceNotFoundException("Order not found with ID: " + orderId));
      notifyOrderConfirmation(order, Set.of(NotificationChannel.EMAIL));
    } catch (Exception e) {
      log.error("Error handling order created event", e);
    }
  }

  @KafkaListener(topics = "payment.processed", groupId = "notification-service")
  public void handlePaymentProcessedEvent(String eventDataJson) {
    try {
      // Parse JSON string to Map
      com.fasterxml.jackson.databind.ObjectMapper mapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      Map<String, Object> eventData = mapper.readValue(eventDataJson, Map.class);

      Long orderId = Long.valueOf(eventData.get("orderId").toString());
      Order order =
          orderRepository
              .findById(orderId)
              .orElseThrow(
                  () -> new ResourceNotFoundException("Order not found with ID: " + orderId));
      String paymentId = eventData.get("paymentId").toString();
      notifyPaymentConfirmation(order, paymentId, Set.of(NotificationChannel.EMAIL));
    } catch (Exception e) {
      log.error("Error handling payment processed event", e);
    }
  }

  // Helper to build notification request with common parameters
  private NotificationRequest buildRequest(
      NotificationType type,
      Set<NotificationChannel> channels,
      String recipient,
      String subject,
      Map<String, Object> data) {
    NotificationRequest request = new NotificationRequest();
    request.setType(type);
    request.setChannels(channels);
    request.setRecipient(recipient);
    request.setSubject(subject);
    request.setTemplate(type.name().toLowerCase().replace('_', '-'));
    request.setData(data);
    request.setPriority(1);
    return request;
  }

  // Build Kafka message payload for notification
  private Map<String, Object> buildNotificationData(
      NotificationRequest request, NotificationChannel channel) {
    return Map.of(
        "notificationId", UUID.randomUUID().toString(),
        "notificationType", request.getType().name(),
        "channel", channel.name(),
        "timestamp", LocalDateTime.now().toString(),
        "recipient", request.getRecipient(),
        "subject", request.getSubject(),
        "template", request.getTemplate(),
        "priority", request.getPriority(),
        "data", request.getData());
  }
}
