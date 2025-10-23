package com.ecommerce.productorder.websocket;

import com.ecommerce.productorder.util.Constants;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StockUpdateBroadcaster {

  private final SimpMessagingTemplate messagingTemplate;

  public StockUpdateBroadcaster(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @KafkaListener(
      topics = "product.stock.updated",
      groupId = "websocket-broadcaster",
      containerFactory = "kafkaListenerContainerFactory")
  public void handleStockUpdateEvent(Map<String, Object> event) {
    try {
      Long productId = ((Number) event.get(Constants.PRODUCT_ID_FIELD)).longValue();
      Integer newStock = ((Number) event.get(Constants.STOCK_QUANTITY_FIELD)).intValue();
      Long timestamp =
          event.containsKey(Constants.TIMESTAMP_FIELD)
              ? ((Number) event.get(Constants.TIMESTAMP_FIELD)).longValue()
              : System.currentTimeMillis();

      log.info("Broadcasting stock update for product ID: {} to {} units", productId, newStock);

      // Create broadcast message
      Map<String, Object> broadcastMessage =
          Map.of(
              Constants.PRODUCT_ID_FIELD, productId,
              Constants.STOCK_QUANTITY_FIELD, newStock,
              Constants.TIMESTAMP_FIELD, timestamp,
              Constants.MESSAGE_FIELD,
                  newStock < Constants.LOW_STOCK_THRESHOLD
                      ? Constants.LOW_STOCK_MESSAGE
                      : Constants.STOCK_UPDATED_MESSAGE);

      // Broadcast to all clients subscribed to this product
      messagingTemplate.convertAndSend(
          Constants.WEBSOCKET_STOCK_TOPIC_PREFIX + productId, broadcastMessage);

      // Broadcast to general stock updates channel
      messagingTemplate.convertAndSend(Constants.WEBSOCKET_STOCK_ALL_TOPIC, broadcastMessage);

      log.debug("Stock update broadcasted successfully for product ID: {}", productId);

    } catch (Exception e) {
      log.error("Error broadcasting stock update event: {}", event, e);
    }
  }

  @KafkaListener(
      topics = "order.created",
      groupId = "websocket-broadcaster",
      containerFactory = "kafkaListenerContainerFactory")
  public void handleOrderCreatedEvent(String eventDataJson) {
    try {
      // Parse JSON string to Map
      com.fasterxml.jackson.databind.ObjectMapper mapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      Map<String, Object> event = mapper.readValue(eventDataJson, Map.class);

      Long customerId = ((Number) event.get(Constants.CUSTOMER_ID_FIELD)).longValue();
      Long orderId = ((Number) event.get(Constants.ORDER_ID_FIELD)).longValue();

      log.info("Broadcasting order created notification to customer ID: {}", customerId);

      Map<String, Object> notification =
          Map.of(
              Constants.ORDER_ID_FIELD,
              orderId,
              Constants.CUSTOMER_ID_FIELD,
              customerId,
              Constants.MESSAGE_FIELD,
              Constants.ORDER_CREATED_MESSAGE,
              Constants.TIMESTAMP_FIELD,
              System.currentTimeMillis());

      // Send to specific customer
      messagingTemplate.convertAndSend(
          Constants.WEBSOCKET_CUSTOMER_ORDERS_TOPIC_PREFIX
              + customerId
              + Constants.WEBSOCKET_CUSTOMER_ORDERS_TOPIC_SUFFIX,
          notification);

    } catch (Exception e) {
      log.error("Error broadcasting order created event: {}", eventDataJson, e);
    }
  }
}
