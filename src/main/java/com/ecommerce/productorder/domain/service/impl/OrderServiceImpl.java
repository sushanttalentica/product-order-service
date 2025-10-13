package com.ecommerce.productorder.domain.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.OrderItem;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.OrderRepository;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import com.ecommerce.productorder.domain.service.OrderService;
import com.ecommerce.productorder.domain.service.ProductService;
import com.ecommerce.productorder.dto.request.CreateOrderRequest;
import com.ecommerce.productorder.dto.response.OrderResponse;
import com.ecommerce.productorder.mapper.OrderMapper;
import com.ecommerce.productorder.events.OrderEventPublisher;
import com.ecommerce.productorder.exception.BusinessException;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import com.ecommerce.productorder.util.Constants;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {
    
    // Constants for concurrency handling
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 50;
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;
    

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer ID: {}", request.getCustomerId());
        
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                return createOrderInternal(request);
            } catch (OptimisticLockException e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    log.error("Failed to create order after {} retries due to high concurrency", MAX_RETRIES);
                    throw new BusinessException("High traffic detected. Please try again in a moment.");
                }
                log.warn("Optimistic lock conflict on attempt {}/{}, retrying...", attempt, MAX_RETRIES);
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException("Order creation interrupted");
                }
            }
        }
        throw new BusinessException("Failed to create order after retries");
    }
    

    private OrderResponse createOrderInternal(CreateOrderRequest request) {
        // Validate request
        validateOrderRequest(request);
        
        // Check product availability and reduce stock atomically
        validateAndReserveProductsAtomic(request);
        
        // Create order entity
        Order order = createOrderEntity(request);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());
        
        // Publish order created event
        orderEventPublisher.publishOrderCreatedEvent(savedOrder);
        
        log.info("Order created successfully for customer ID: {}", request.getCustomerId());
        return orderMapper.toResponse(savedOrder);
    }
    

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderById(Long orderId) {
        log.debug("Retrieving order by ID: {}", orderId);
        
        return orderRepository.findById(orderId)
                .map(orderMapper::toResponse);
    }
    

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        log.debug("Retrieving orders by customer ID: {}", customerId);
        
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(orderMapper::toResponse);
    }
    

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(String status, Pageable pageable) {
        log.debug("Retrieving orders by status: {}", status);
        
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatus(orderStatus, pageable)
                    .map(orderMapper::toResponse);
        } catch (IllegalArgumentException e) {
            log.error("Invalid order status: {}", status, e);
            throw new BusinessException("Invalid order status: " + status);
        }
    }
    

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        log.info("Updating order status for order ID: {} to {}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        try {
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            order.updateStatus(newStatus);
            Order updatedOrder = orderRepository.save(order);
            
            // Publish order status updated event
            orderEventPublisher.publishOrderStatusUpdatedEvent(updatedOrder);
            
            log.info("Order status updated successfully for order ID: {}", orderId);
            return orderMapper.toResponse(updatedOrder);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid order status: {}", status, e);
            throw new BusinessException("Invalid order status: " + status);
        } catch (Exception e) {
            log.error("Error updating order status for order ID: {}", orderId, e);
            throw new BusinessException("Failed to update order status: " + e.getMessage());
        }
    }
    

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        log.info("Cancelling order with ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        try {
            // Validate cancellation
            if (!order.canBeCancelled()) {
                throw new BusinessException("Order cannot be cancelled in current state: " + order.getStatus());
            }
            
            // Restore product stock
            restoreProductStock(order);
            
            // Update order status
            order.updateStatus(Order.OrderStatus.CANCELLED);
            Order updatedOrder = orderRepository.save(order);
            
            // Publish order cancelled event
            orderEventPublisher.publishOrderCancelledEvent(updatedOrder);
            
            log.info("Order cancelled successfully for order ID: {}", orderId);
            return orderMapper.toResponse(updatedOrder);
            
        } catch (Exception e) {
            log.error("Error cancelling order ID: {}", orderId, e);
            throw new BusinessException("Failed to cancel order: " + e.getMessage());
        }
    }
    

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.debug("Retrieving all orders with pagination: {}", pageable);
        
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }
    

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByDateRange(String startDate, String endDate, Pageable pageable) {
        log.debug("Retrieving orders by date range: {} to {}", startDate, endDate);
        
        try {
            LocalDateTime start = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime end = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            return orderRepository.findByCreatedAtBetween(start, end, pageable)
                    .map(orderMapper::toResponse);
        } catch (Exception e) {
            log.error("Error parsing date range: {} to {}", startDate, endDate, e);
            throw new BusinessException("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss");
        }
    }
    

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByAmountRange(String minAmount, String maxAmount, Pageable pageable) {
        log.debug("Retrieving orders by amount range: {} to {}", minAmount, maxAmount);
        
        try {
            java.math.BigDecimal min = new java.math.BigDecimal(minAmount);
            java.math.BigDecimal max = new java.math.BigDecimal(maxAmount);
            
            return orderRepository.findByTotalAmountBetween(min, max, pageable)
                    .map(orderMapper::toResponse);
        } catch (NumberFormatException e) {
            log.error("Error parsing amount range: {} to {}", minAmount, maxAmount, e);
            throw new BusinessException("Invalid amount format. Use decimal format: 0.00");
        }
    }
    

    @Override
    @Transactional(readOnly = true)
    public Object getOrderStatistics(String startDate, String endDate) {
        log.debug("Retrieving order statistics for date range: {} to {}", startDate, endDate);
        
        try {
            // This would typically involve complex queries and aggregations
            // For now, returning a simple statistics object
            return java.util.Map.of(
                "totalOrders", orderRepository.count(),
                "totalRevenue", orderRepository.findAll().stream()
                    .map(Order::getTotalAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add),
                "averageOrderValue", orderRepository.findAll().stream()
                    .map(Order::getTotalAmount)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)
                    .divide(new java.math.BigDecimal(orderRepository.count()), 2, java.math.RoundingMode.HALF_UP)
            );
        } catch (Exception e) {
            log.error("Error calculating order statistics", e);
            throw new BusinessException("Failed to calculate order statistics: " + e.getMessage());
        }
    }
    

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersNeedingAttention() {
        log.debug("Retrieving orders needing attention");
        
        // This would typically involve complex business logic
        // For now, returning orders that are pending for more than 24 hours
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(Constants.HOURS_FOR_ATTENTION_CHECK);
        
        return orderRepository.findByStatusAndCreatedAtBefore(Order.OrderStatus.PENDING, cutoffTime)
                .stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }
    

    private void validateOrderRequest(CreateOrderRequest request) {
        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new BusinessException("Order must contain at least one item");
        }
        
        // Calculate actual total amount based on products and quantities
        BigDecimal calculatedTotal = calculateOrderTotal(request);
        if (calculatedTotal.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Order total amount must be greater than zero");
        }
    }
    

    private void validateAndReserveProductsAtomic(CreateOrderRequest request) {
        for (var orderItem : request.getOrderItems()) {
            // First, check if product exists and is available
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + orderItem.getProductId()));
            
            if (!product.isAvailableForPurchase()) {
                throw new BusinessException("Product is not available for purchase: " + product.getName());
            }
            
            // Atomic stock reduction - prevents race conditions
            int rowsUpdated = productRepository.reduceStockAtomic(
                orderItem.getProductId(),
                orderItem.getQuantity()
            );
            
            if (rowsUpdated == 0) {
                // Atomic update failed - either product deleted or insufficient stock
                // Re-fetch to get current stock for error message
                Product currentProduct = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + orderItem.getProductId()));
                
                throw new BusinessException(String.format(
                    "Insufficient stock for product: %s (Available: %d, Requested: %d)",
                    currentProduct.getName(),
                    currentProduct.getStockQuantity(),
                    orderItem.getQuantity()
                ));
            }
            
            log.info("Atomically reduced stock for product ID: {} by {}", 
                orderItem.getProductId(), orderItem.getQuantity());
        }
    }
    

    @Deprecated
    private void validateAndReserveProducts(CreateOrderRequest request) {
        for (var orderItem : request.getOrderItems()) {
            // Use pessimistic write lock to prevent concurrent modifications
            Product product = productRepository.findByIdWithLock(orderItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + orderItem.getProductId()));
            
            if (!product.isAvailableForPurchase()) {
                throw new BusinessException("Product is not available for purchase: " + product.getName());
            }
            
            if (!product.hasSufficientStock(orderItem.getQuantity())) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }
            
            // Reserve stock (lock prevents race conditions)
            product.reduceStock(orderItem.getQuantity());
            productRepository.save(product);
            // Lock released when transaction commits
        }
    }
    

    private Order createOrderEntity(CreateOrderRequest request) {
        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .customerId(request.getCustomerId())
                .customerEmail(request.getCustomerEmail())
                .status(Order.OrderStatus.PENDING)
                .totalAmount(calculateOrderTotal(request))
                .shippingAddress(request.getShippingAddress())
                .build();
        
        // Create and associate order items
        List<OrderItem> orderItems = request.getOrderItems().stream()
                .map(orderItemRequest -> {
                    Product product = productRepository.findById(orderItemRequest.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + orderItemRequest.getProductId()));
                    
                    return OrderItem.create(order, product, orderItemRequest.getQuantity());
                })
                .collect(Collectors.toList());
        
        order.setOrderItems(orderItems);
        return order;
    }
    

    private BigDecimal calculateOrderTotal(CreateOrderRequest request) {
        return request.getOrderItems().stream()
                .map(this::calculateItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    

    private BigDecimal calculateItemTotal(CreateOrderRequest.OrderItemRequest orderItem) {
        Product product = findProductById(orderItem.getProductId());
        return product.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
    }
    

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }
    

    private void restoreProductStock(Order order) {
        log.info("Restoring product stock for order ID: {}", order.getId());
        
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            log.warn("Order {} has no items to restore stock for", order.getId());
            return;
        }
        
        for (OrderItem orderItem : order.getOrderItems()) {
            try {
                // Restore stock for each product in the order
                productService.restoreProductStock(orderItem.getProduct().getId(), orderItem.getQuantity());
                log.info("Restored {} units of product {} (ID: {})", 
                    orderItem.getQuantity(), 
                    orderItem.getProduct().getName(), 
                    orderItem.getProduct().getId());
            } catch (Exception e) {
                log.error("Failed to restore stock for product ID: {} in order ID: {}", 
                    orderItem.getProduct().getId(), order.getId(), e);
                // Continue with other products even if one fails
            }
        }
        
        log.info("Completed stock restoration for order ID: {}", order.getId());
    }
}
