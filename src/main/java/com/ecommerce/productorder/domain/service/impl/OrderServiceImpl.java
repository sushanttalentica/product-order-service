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
import com.ecommerce.productorder.service.OrderEventPublisher;
import com.ecommerce.productorder.exception.BusinessException;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of OrderService
 * 
 * Design Principles Applied:
 * - Service Layer Pattern: Encapsulates business logic
 * - Single Responsibility: Only handles order business logic
 * - Dependency Injection: Uses constructor injection for dependencies
 * - Transaction Management: Uses @Transactional for data consistency
 * - Exception Handling: Proper exception handling with custom exceptions
 * - Logging: Uses SLF4J for comprehensive logging
 * - Stream API: Uses Java Streams for data processing
 * - Optional: Uses Optional for null-safe operations
 * - Builder Pattern: Uses Builder pattern for object creation
 * - Factory Pattern: Uses static factory methods
 * - Command Query Separation: Separates read and write operations
 * - Event-Driven Architecture: Publishes events for downstream workflows
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;
    
    /**
     * Creates a new order
     * Handles order creation and triggers downstream workflows
     * 
     * @param request the order creation request
     * @return OrderResponse containing order details
     * @throws BusinessException if order creation fails
     */
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer ID: {}", request.getCustomerId());
        
        try {
            // Validate request
            validateOrderRequest(request);
            
            // Check product availability and reduce stock
            validateAndReserveProducts(request);
            
            // Create order entity
            Order order = createOrderEntity(request);
            
            // Save order
            Order savedOrder = orderRepository.save(order);
            log.info("Order created with ID: {}", savedOrder.getId());
            
            // Publish order created event
            orderEventPublisher.publishOrderCreatedEvent(savedOrder);
            
            log.info("Order created successfully for customer ID: {}", request.getCustomerId());
            return orderMapper.toResponse(savedOrder);
            
        } catch (Exception e) {
            log.error("Error creating order for customer ID: {}", request.getCustomerId(), e);
            throw new BusinessException("Failed to create order: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves order by ID
     * Uses Optional for null-safe operations
     * 
     * @param orderId the order ID to search for
     * @return Optional containing order if found, empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<OrderResponse> getOrderById(Long orderId) {
        log.debug("Retrieving order by ID: {}", orderId);
        
        return orderRepository.findById(orderId)
                .map(orderMapper::toResponse);
    }
    
    /**
     * Retrieves orders by customer ID
     * Uses Java Streams for data processing
     * 
     * @param customerId the customer ID to search for
     * @param pageable the pagination parameters
     * @return Page of orders for the customer
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        log.debug("Retrieving orders by customer ID: {}", customerId);
        
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(orderMapper::toResponse);
    }
    
    /**
     * Retrieves orders by status
     * Uses Java Streams for data processing
     * 
     * @param status the order status to search for
     * @param pageable the pagination parameters
     * @return Page of orders with the specified status
     */
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
    
    /**
     * Updates order status
     * Handles order status updates and triggers downstream workflows
     * 
     * @param orderId the order ID to update
     * @param status the new status
     * @return OrderResponse containing updated order details
     * @throws BusinessException if status update fails
     */
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
    
    /**
     * Cancels an order
     * Handles order cancellation and triggers downstream workflows
     * 
     * @param orderId the order ID to cancel
     * @return OrderResponse containing updated order details
     * @throws BusinessException if cancellation fails
     */
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
    
    /**
     * Retrieves all orders with pagination
     * Uses Java Streams for data processing
     * 
     * @param pageable the pagination parameters
     * @return Page of all orders
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.debug("Retrieving all orders with pagination: {}", pageable);
        
        return orderRepository.findAll(pageable)
                .map(orderMapper::toResponse);
    }
    
    /**
     * Retrieves orders by date range
     * Uses Java Streams for data processing
     * 
     * @param startDate the start date for the range
     * @param endDate the end date for the range
     * @param pageable the pagination parameters
     * @return Page of orders within the date range
     */
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
    
    /**
     * Retrieves orders by amount range
     * Uses Java Streams for data processing
     * 
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @param pageable the pagination parameters
     * @return Page of orders within the amount range
     */
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
    
    /**
     * Gets order statistics
     * Returns order statistics for analysis
     * 
     * @param startDate the start date for statistics (optional)
     * @param endDate the end date for statistics (optional)
     * @return Object containing order statistics
     */
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
    
    /**
     * Gets orders needing attention
     * Returns list of orders that need attention
     * 
     * @return List of orders needing attention
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersNeedingAttention() {
        log.debug("Retrieving orders needing attention");
        
        // This would typically involve complex business logic
        // For now, returning orders that are pending for more than 24 hours
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        
        return orderRepository.findByStatusAndCreatedAtBefore(Order.OrderStatus.PENDING, cutoffTime)
                .stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Validates order request
     * Encapsulates validation logic
     * 
     * @param request the order request to validate
     * @throws BusinessException if validation fails
     */
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
    
    /**
     * Validates and reserves products
     * Encapsulates product validation and stock reservation logic
     * 
     * @param request the order request
     * @throws BusinessException if validation fails
     */
    private void validateAndReserveProducts(CreateOrderRequest request) {
        for (var orderItem : request.getOrderItems()) {
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + orderItem.getProductId()));
            
            if (!product.isAvailableForPurchase()) {
                throw new BusinessException("Product is not available for purchase: " + product.getName());
            }
            
            if (!product.hasSufficientStock(orderItem.getQuantity())) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }
            
            // Reserve stock
            product.reduceStock(orderItem.getQuantity());
            productRepository.save(product);
        }
    }
    
    /**
     * Creates order entity from request
     * Uses Builder pattern for object creation
     * 
     * @param request the order request
     * @return Order entity
     */
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
    
    /**
     * Calculates the total amount for an order
     * Fetches product prices and calculates based on quantities
     * 
     * @param request the order request
     * @return calculated total amount
     */
    private BigDecimal calculateOrderTotal(CreateOrderRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        
        for (var orderItem : request.getOrderItems()) {
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + orderItem.getProductId()));
            
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            total = total.add(itemTotal);
        }
        
        return total;
    }
    
    /**
     * Restores product stock
     * Encapsulates stock restoration logic
     * 
     * @param order the order to restore stock for
     */
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
