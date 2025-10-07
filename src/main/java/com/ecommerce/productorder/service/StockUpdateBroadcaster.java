package com.ecommerce.productorder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Stock Update Broadcaster
 * Listens to Kafka stock update events and broadcasts to WebSocket clients
 * 
 * Design Principles Applied:
 * - Event-Driven Architecture: Consumes Kafka events
 * - Publish-Subscribe Pattern: Broadcasts to WebSocket subscribers
 * - Single Responsibility: Only handles stock update broadcasting
 * - Loose Coupling: Decoupled from business logic
 * - Real-Time Communication: Sub-second latency for stock updates
 * 
 * Flow:
 * 1. Product stock changes (order placed/cancelled)
 * 2. Kafka event "product.stock.updated" published
 * 3. This service consumes the event
 * 4. Broadcasts to all WebSocket clients subscribed to that product
 * 5. Frontend updates UI in real-time
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockUpdateBroadcaster {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Handles stock update events from Kafka
     * Broadcasts updates to all connected WebSocket clients
     * 
     * @param event the stock update event containing productId and new stock quantity
     */
    @KafkaListener(
        topics = "product.stock.updated",
        groupId = "websocket-broadcaster",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStockUpdateEvent(Map<String, Object> event) {
        try {
            Long productId = ((Number) event.get("productId")).longValue();
            Integer newStock = ((Number) event.get("stockQuantity")).intValue();
            Long timestamp = event.containsKey("timestamp") ? 
                ((Number) event.get("timestamp")).longValue() : System.currentTimeMillis();
            
            log.info("Broadcasting stock update for product ID: {} to {} units", productId, newStock);
            
            // Create broadcast message
            Map<String, Object> broadcastMessage = Map.of(
                "productId", productId,
                "stockQuantity", newStock,
                "timestamp", timestamp,
                "message", newStock < 10 ? "Low stock!" : "Stock updated"
            );
            
            // Broadcast to all clients subscribed to this product
            messagingTemplate.convertAndSend("/topic/stock/" + productId, broadcastMessage);
            
            // Also broadcast to general stock updates channel
            messagingTemplate.convertAndSend("/topic/stock/all", broadcastMessage);
            
            log.debug("Stock update broadcasted successfully for product ID: {}", productId);
            
        } catch (Exception e) {
            log.error("Error broadcasting stock update event: {}", event, e);
            // Don't throw - graceful degradation (stock update broadcast is not critical)
        }
    }
    
    /**
     * Handles order created events
     * Can broadcast order confirmations to customers
     * 
     * @param event the order created event
     */
    @KafkaListener(
        topics = "order.created",
        groupId = "websocket-broadcaster",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderCreatedEvent(Map<String, Object> event) {
        try {
            Long customerId = ((Number) event.get("customerId")).longValue();
            Long orderId = ((Number) event.get("orderId")).longValue();
            
            log.info("Broadcasting order created notification to customer ID: {}", customerId);
            
            Map<String, Object> notification = Map.of(
                "orderId", orderId,
                "customerId", customerId,
                "message", "Your order has been created successfully!",
                "timestamp", System.currentTimeMillis()
            );
            
            // Send to specific customer
            messagingTemplate.convertAndSend("/topic/customer/" + customerId + "/orders", notification);
            
        } catch (Exception e) {
            log.error("Error broadcasting order created event: {}", event, e);
        }
    }
}

