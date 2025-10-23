package com.ecommerce.productorder.inventory.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.OrderRepository;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import com.ecommerce.productorder.inventory.service.InventoryService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class InventoryServiceImpl implements InventoryService {

  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  public InventoryServiceImpl(
      ProductRepository productRepository,
      OrderRepository orderRepository,
      KafkaTemplate<String, Object> kafkaTemplate) {
    this.productRepository = productRepository;
    this.orderRepository = orderRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  // Kafka topics for inventory events
  private static final String INVENTORY_RESERVED_TOPIC = "inventory.reserved";
  private static final String INVENTORY_RELEASED_TOPIC = "inventory.released";
  private static final String INVENTORY_UPDATED_TOPIC = "inventory.updated";
  private static final String INVENTORY_LOW_STOCK_TOPIC = "inventory.low-stock";

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
        Product product =
            productRepository
                .findById(orderItem.getProduct().getId())
                .orElseThrow(
                    () ->
                        new RuntimeException(
                            "Product not found: " + orderItem.getProduct().getId()));

        if (!product.hasSufficientStock(orderItem.getQuantity())) {
          log.warn(
              "Insufficient stock for product ID: {}, required: {}, available: {}",
              product.getId(),
              orderItem.getQuantity(),
              product.getStockQuantity());
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
        Product product =
            productRepository
                .findById(orderItem.getProduct().getId())
                .orElseThrow(
                    () ->
                        new RuntimeException(
                            "Product not found: " + orderItem.getProduct().getId()));

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
        Product product =
            productRepository
                .findById(orderItem.getProduct().getId())
                .orElseThrow(
                    () ->
                        new RuntimeException(
                            "Product not found: " + orderItem.getProduct().getId()));

        // Update stock (already reduced during reservation)
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

  @Override
  @Transactional(readOnly = true)
  public boolean checkProductAvailability(Long productId, Integer quantity) {
    log.debug(
        "Checking product availability for product ID: {}, quantity: {}", productId, quantity);

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

  @Override
  @Transactional(readOnly = true)
  public Optional<Integer> getProductStockLevel(Long productId) {
    log.debug("Getting product stock level for product ID: {}", productId);

    try {
      return productRepository.findById(productId).map(Product::getStockQuantity);

    } catch (Exception e) {
      log.error("Error getting product stock level for product ID: {}", productId, e);
      return Optional.empty();
    }
  }

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

  @Override
  @Transactional(readOnly = true)
  public List<Product> getLowStockProducts(Integer threshold) {
    log.debug("Getting low stock products with threshold: {}", threshold);

    try {
      List<Product> lowStockProducts =
          productRepository.findAll().stream()
              .filter(product -> product.getStockQuantity() <= threshold)
              .collect(Collectors.toList());

      log.debug("Found {} products with low stock", lowStockProducts.size());
      return lowStockProducts;

    } catch (Exception e) {
      log.error("Error getting low stock products", e);
      return List.of();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> getOutOfStockProducts() {
    log.debug("Getting out of stock products");

    try {
      List<Product> outOfStockProducts =
          productRepository.findAll().stream()
              .filter(product -> product.getStockQuantity() == 0)
              .collect(Collectors.toList());

      log.debug("Found {} out of stock products", outOfStockProducts.size());
      return outOfStockProducts;

    } catch (Exception e) {
      log.error("Error getting out of stock products", e);
      return List.of();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Object getInventoryStatistics() {
    log.debug("Getting inventory statistics");

    try {
      List<Product> allProducts = productRepository.findAll();

      Map<String, Object> statistics = new HashMap<>();
      statistics.put("totalProducts", allProducts.size());
      statistics.put(
          "totalStockValue",
          allProducts.stream()
              .mapToDouble(product -> product.getPrice().doubleValue() * product.getStockQuantity())
              .sum());
      statistics.put(
          "averageStockLevel",
          allProducts.stream().mapToInt(Product::getStockQuantity).average().orElse(0.0));
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

  @KafkaListener(topics = "order.created", groupId = "inventory-service")
  public void handleOrderCreatedEvent(String eventDataJson) {
    log.info("Received order created event: {}", eventDataJson);

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

      reserveInventory(order);

    } catch (Exception e) {
      log.error("Error handling order created event", e);
    }
  }

  @KafkaListener(topics = "order.cancelled", groupId = "inventory-service")
  public void handleOrderCancelledEvent(Map<String, Object> eventData) {
    log.info("Received order cancelled event: {}", eventData);

    try {
      Long orderId = Long.valueOf(eventData.get("orderId").toString());
      Order order =
          orderRepository
              .findById(orderId)
              .orElseThrow(
                  () -> new ResourceNotFoundException("Order not found with ID: " + orderId));

      releaseInventory(order);

    } catch (Exception e) {
      log.error("Error handling order cancelled event", e);
    }
  }

  private void publishInventoryReservedEvent(Order order) {
    try {
      Map<String, Object> eventData = createInventoryEventData(order, "INVENTORY_RESERVED");
      kafkaTemplate.send(INVENTORY_RESERVED_TOPIC, order.getId().toString(), eventData);

      log.info("Inventory reserved event published for order ID: {}", order.getId());
    } catch (Exception e) {
      log.error("Error publishing inventory reserved event for order ID: {}", order.getId(), e);
    }
  }

  private void publishInventoryReleasedEvent(Order order) {
    try {
      Map<String, Object> eventData = createInventoryEventData(order, "INVENTORY_RELEASED");
      kafkaTemplate.send(INVENTORY_RELEASED_TOPIC, order.getId().toString(), eventData);

      log.info("Inventory released event published for order ID: {}", order.getId());
    } catch (Exception e) {
      log.error("Error publishing inventory released event for order ID: {}", order.getId(), e);
    }
  }

  private void publishInventoryUpdatedEvent(Order order) {
    try {
      Map<String, Object> eventData = createInventoryEventData(order, "INVENTORY_UPDATED");
      kafkaTemplate.send(INVENTORY_UPDATED_TOPIC, order.getId().toString(), eventData);

      log.info("Inventory updated event published for order ID: {}", order.getId());
    } catch (Exception e) {
      log.error("Error publishing inventory updated event for order ID: {}", order.getId(), e);
    }
  }

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
}
