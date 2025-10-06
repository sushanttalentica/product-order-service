package com.ecommerce.productorder.inventory.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import com.ecommerce.productorder.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of InventoryService
 * Handles inventory management and integrates with external inventory services
 * 
 * Design Principles Applied:
 * - Service Layer Pattern: Encapsulates inventory business logic
 * - Single Responsibility: Only handles inventory operations
 * - Dependency Injection: Uses constructor injection for dependencies
 * - Event-Driven Architecture: Listens to Kafka events for inventory updates
 * - Logging: Uses SLF4J for comprehensive logging
 * - Exception Handling: Proper exception handling with graceful degradation
 * - Optional: Uses Optional for null-safe operations
 * - Builder Pattern: Uses Builder pattern for object creation
 * - Factory Pattern: Uses static factory methods for event creation
 * - Stream API: Uses Java Streams for data processing
 * - Transaction Management: Uses @Transactional for data consistency
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryServiceImpl implements InventoryService {
    
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    // Kafka topics for inventory events
    private static final String INVENTORY_RESERVED_TOPIC = "inventory.reserved";
    private static final String INVENTORY_RELEASED_TOPIC = "inventory.released";
    private static final String INVENTORY_UPDATED_TOPIC = "inventory.updated";
    private static final String INVENTORY_LOW_STOCK_TOPIC = "inventory.low-stock";
    
    /**
     * Reserves inventory for order
     * Reserves product stock for order items
     * 
     * @param order the order entity
     * @return true if inventory reserved successfully, false otherwise
     */
    @Override
    @Transactional
    public boolean reserveInventory(Order order) {
        log.info("Reserving inventory for order ID: {}", order.getId());
        
        try {
            // Validate order
            if (order == null || order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                log.warn("Invalid order for inventory reservation: {}", order);
                return false;
            }
            
            // Reserve inventory for each order item
            for (var orderItem : order.getOrderItems()) {
                Product product = productRepository.findById(orderItem.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + orderItem.getProduct().getId()));
                
                if (!product.hasSufficientStock(orderItem.getQuantity())) {
                    log.warn("Insufficient stock for product ID: {}, required: {}, available: {}", 
                            product.getId(), orderItem.getQuantity(), product.getStockQuantity());
                    return false;
                }
                
                // Reserve stock
                product.reduceStock(orderItem.getQuantity());
                productRepository.save(product);
                
                log.info("Reserved {} units of product ID: {}", orderItem.getQuantity(), product.getId());
            }
            
            // Publish inventory reserved event
            publishInventoryReservedEvent(order);
            
            log.info("Inventory reserved successfully for order ID: {}", order.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error reserving inventory for order ID: {}", order.getId(), e);
            return false;
        }
    }
    
    /**
     * Releases inventory for order
     * Releases reserved stock for order cancellation
     * 
     * @param order the order entity
     * @return true if inventory released successfully, false otherwise
     */
    @Override
    @Transactional
    public boolean releaseInventory(Order order) {
        log.info("Releasing inventory for order ID: {}", order.getId());
        
        try {
            // Validate order
            if (order == null || order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                log.warn("Invalid order for inventory release: {}", order);
                return false;
            }
            
            // Release inventory for each order item
            for (var orderItem : order.getOrderItems()) {
                Product product = productRepository.findById(orderItem.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + orderItem.getProduct().getId()));
                
                // Restore stock
                product.restoreStock(orderItem.getQuantity());
                productRepository.save(product);
                
                log.info("Released {} units of product ID: {}", orderItem.getQuantity(), product.getId());
            }
            
            // Publish inventory released event
            publishInventoryReleasedEvent(order);
            
            log.info("Inventory released successfully for order ID: {}", order.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error releasing inventory for order ID: {}", order.getId(), e);
            return false;
        }
    }
    
    /**
     * Updates inventory after order completion
     * Updates stock levels after successful order processing
     * 
     * @param order the order entity
     * @return true if inventory updated successfully, false otherwise
     */
    @Override
    @Transactional
    public boolean updateInventoryAfterOrder(Order order) {
        log.info("Updating inventory after order completion for order ID: {}", order.getId());
        
        try {
            // Validate order
            if (order == null || order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                log.warn("Invalid order for inventory update: {}", order);
                return false;
            }
            
            // Update inventory for each order item
            for (var orderItem : order.getOrderItems()) {
                Product product = productRepository.findById(orderItem.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + orderItem.getProduct().getId()));
                
                // Update stock (already reduced during reservation)
                // This is where you might update additional inventory metrics
                productRepository.save(product);
                
                log.info("Updated inventory for product ID: {}", product.getId());
            }
            
            // Publish inventory updated event
            publishInventoryUpdatedEvent(order);
            
            log.info("Inventory updated successfully for order ID: {}", order.getId());
            return true;
            
        } catch (Exception e) {
            log.error("Error updating inventory for order ID: {}", order.getId(), e);
            return false;
        }
    }
    
    /**
     * Checks product availability
     * Verifies if product has sufficient stock
     * 
     * @param productId the product ID
     * @param quantity the required quantity
     * @return true if product is available, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean checkProductAvailability(Long productId, Integer quantity) {
        log.debug("Checking product availability for product ID: {}, quantity: {}", productId, quantity);
        
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            
            if (productOpt.isEmpty()) {
                log.warn("Product not found: {}", productId);
                return false;
            }
            
            Product product = productOpt.get();
            boolean available = product.hasSufficientStock(quantity);
            
            log.debug("Product availability for product ID: {} - {}", productId, available);
            return available;
            
        } catch (Exception e) {
            log.error("Error checking product availability for product ID: {}", productId, e);
            return false;
        }
    }
    
    /**
     * Gets product stock level
     * Retrieves current stock level for product
     * 
     * @param productId the product ID
     * @return Optional containing stock level if found, empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> getProductStockLevel(Long productId) {
        log.debug("Getting product stock level for product ID: {}", productId);
        
        try {
            return productRepository.findById(productId)
                    .map(Product::getStockQuantity);
            
        } catch (Exception e) {
            log.error("Error getting product stock level for product ID: {}", productId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Updates product stock level
     * Updates stock level for product
     * 
     * @param productId the product ID
     * @param newStockLevel the new stock level
     * @return true if stock updated successfully, false otherwise
     */
    @Override
    @Transactional
    public boolean updateProductStockLevel(Long productId, Integer newStockLevel) {
        log.info("Updating product stock level for product ID: {} to {}", productId, newStockLevel);
        
        try {
            Optional<Product> productOpt = productRepository.findById(productId);
            
            if (productOpt.isEmpty()) {
                log.warn("Product not found: {}", productId);
                return false;
            }
            
            Product product = productOpt.get();
            product.setStockQuantity(newStockLevel);
            productRepository.save(product);
            
            log.info("Product stock level updated successfully for product ID: {}", productId);
            return true;
            
        } catch (Exception e) {
            log.error("Error updating product stock level for product ID: {}", productId, e);
            return false;
        }
    }
    
    /**
     * Gets low stock products
     * Retrieves products with stock below threshold
     * 
     * @param threshold the stock threshold
     * @return List of products with low stock
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        log.debug("Getting low stock products with threshold: {}", threshold);
        
        try {
            List<Product> lowStockProducts = productRepository.findAll()
                    .stream()
                    .filter(product -> product.getStockQuantity() <= threshold)
                    .collect(Collectors.toList());
            
            log.debug("Found {} products with low stock", lowStockProducts.size());
            return lowStockProducts;
            
        } catch (Exception e) {
            log.error("Error getting low stock products", e);
            return List.of();
        }
    }
    
    /**
     * Gets out of stock products
     * Retrieves products with zero stock
     * 
     * @return List of out of stock products
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> getOutOfStockProducts() {
        log.debug("Getting out of stock products");
        
        try {
            List<Product> outOfStockProducts = productRepository.findAll()
                    .stream()
                    .filter(product -> product.getStockQuantity() == 0)
                    .collect(Collectors.toList());
            
            log.debug("Found {} out of stock products", outOfStockProducts.size());
            return outOfStockProducts;
            
        } catch (Exception e) {
            log.error("Error getting out of stock products", e);
            return List.of();
        }
    }
    
    /**
     * Gets inventory statistics
     * Retrieves inventory statistics and metrics
     * 
     * @return Object containing inventory statistics
     */
    @Override
    @Transactional(readOnly = true)
    public Object getInventoryStatistics() {
        log.debug("Getting inventory statistics");
        
        try {
            List<Product> allProducts = productRepository.findAll();
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalProducts", allProducts.size());
            statistics.put("totalStockValue", allProducts.stream()
                    .mapToDouble(product -> product.getPrice().doubleValue() * product.getStockQuantity())
                    .sum());
            statistics.put("averageStockLevel", allProducts.stream()
                    .mapToInt(Product::getStockQuantity)
                    .average()
                    .orElse(0.0));
            statistics.put("lowStockProducts", getLowStockProducts(10).size());
            statistics.put("outOfStockProducts", getOutOfStockProducts().size());
            statistics.put("timestamp", LocalDateTime.now().toString());
            
            log.debug("Inventory statistics calculated successfully");
            return statistics;
            
        } catch (Exception e) {
            log.error("Error getting inventory statistics", e);
            return Map.of("error", "Failed to calculate inventory statistics");
        }
    }
    
    /**
     * Kafka listener for order events
     * Listens to order events and triggers inventory updates
     * 
     * @param eventData the event data
     */
    @KafkaListener(topics = "order.created", groupId = "inventory-service")
    public void handleOrderCreatedEvent(Map<String, Object> eventData) {
        log.info("Received order created event: {}", eventData);
        
        try {
            // Extract order information from event
            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            
            // Create mock order for inventory processing
            Order order = createMockOrder(orderId);
            
            // Reserve inventory
            reserveInventory(order);
            
        } catch (Exception e) {
            log.error("Error handling order created event", e);
        }
    }
    
    /**
     * Kafka listener for order cancellation events
     * Listens to order cancellation events and triggers inventory release
     * 
     * @param eventData the event data
     */
    @KafkaListener(topics = "order.cancelled", groupId = "inventory-service")
    public void handleOrderCancelledEvent(Map<String, Object> eventData) {
        log.info("Received order cancelled event: {}", eventData);
        
        try {
            // Extract order information from event
            Long orderId = Long.valueOf(eventData.get("orderId").toString());
            
            // Create mock order for inventory processing
            Order order = createMockOrder(orderId);
            
            // Release inventory
            releaseInventory(order);
            
        } catch (Exception e) {
            log.error("Error handling order cancelled event", e);
        }
    }
    
    /**
     * Publishes inventory reserved event
     * Publishes event when inventory is reserved
     * 
     * @param order the order entity
     */
    private void publishInventoryReservedEvent(Order order) {
        try {
            Map<String, Object> eventData = createInventoryEventData(order, "INVENTORY_RESERVED");
            kafkaTemplate.send(INVENTORY_RESERVED_TOPIC, order.getId().toString(), eventData);
            
            log.info("Inventory reserved event published for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing inventory reserved event for order ID: {}", order.getId(), e);
        }
    }
    
    /**
     * Publishes inventory released event
     * Publishes event when inventory is released
     * 
     * @param order the order entity
     */
    private void publishInventoryReleasedEvent(Order order) {
        try {
            Map<String, Object> eventData = createInventoryEventData(order, "INVENTORY_RELEASED");
            kafkaTemplate.send(INVENTORY_RELEASED_TOPIC, order.getId().toString(), eventData);
            
            log.info("Inventory released event published for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing inventory released event for order ID: {}", order.getId(), e);
        }
    }
    
    /**
     * Publishes inventory updated event
     * Publishes event when inventory is updated
     * 
     * @param order the order entity
     */
    private void publishInventoryUpdatedEvent(Order order) {
        try {
            Map<String, Object> eventData = createInventoryEventData(order, "INVENTORY_UPDATED");
            kafkaTemplate.send(INVENTORY_UPDATED_TOPIC, order.getId().toString(), eventData);
            
            log.info("Inventory updated event published for order ID: {}", order.getId());
        } catch (Exception e) {
            log.error("Error publishing inventory updated event for order ID: {}", order.getId(), e);
        }
    }
    
    /**
     * Creates inventory event data
     * Factory method for event data creation
     * 
     * @param order the order entity
     * @param eventType the type of event
     * @return Map containing event data
     */
    private Map<String, Object> createInventoryEventData(Order order, String eventType) {
        Map<String, Object> eventData = new HashMap<>();
        
        eventData.put("eventType", eventType);
        eventData.put("eventId", UUID.randomUUID().toString());
        eventData.put("timestamp", LocalDateTime.now().toString());
        eventData.put("orderId", order.getId());
        eventData.put("orderNumber", order.getOrderNumber());
        eventData.put("customerId", order.getCustomerId());
        eventData.put("totalAmount", order.getTotalAmount());
        
        // Service information
        eventData.put("serviceName", "product-order-service");
        eventData.put("serviceVersion", "1.0.0");
        
        return eventData;
    }
    
    /**
     * Creates mock order for inventory processing
     * Factory method for order creation
     * 
     * @param orderId the order ID
     * @return Order entity
     */
    private Order createMockOrder(Long orderId) {
        // This would typically fetch the order from the database
        // For now, create a mock order
        return Order.builder()
                .id(orderId)
                .orderNumber("ORD-" + orderId)
                .customerId(1L)
                .customerEmail("customer@example.com")
                .status(Order.OrderStatus.PENDING)
                .totalAmount(java.math.BigDecimal.valueOf(100.00))
                .build();
    }
}
