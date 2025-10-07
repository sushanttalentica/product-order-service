package com.ecommerce.productorder.domain.service;

import com.ecommerce.productorder.dto.request.CreateOrderRequest;
import com.ecommerce.productorder.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Order operations
 * 
 * Design Principles Applied:
 * - Interface Segregation: Defines only necessary order operations
 * - Single Responsibility: Only handles order business logic
 * - Dependency Inversion: Depends on abstractions, not implementations
 * - Command Query Separation: Separates read and write operations
 * - Optional Return Types: Uses Optional for null-safe operations
 * - Business Logic Encapsulation: Encapsulates order business rules
 */
public interface OrderService {
    
    /**
     * Creates a new order
     * Handles order creation and triggers downstream workflows
     * 
     * @param request the order creation request
     * @return OrderResponse containing order details
     * @throws IllegalArgumentException if request is invalid
     * @throws IllegalStateException if order cannot be created
     */
    OrderResponse createOrder(CreateOrderRequest request);
    
    /**
     * Retrieves order by ID
     * Uses Optional for null-safe operations
     * 
     * @param orderId the order ID to search for
     * @return Optional containing order if found, empty otherwise
     */
    Optional<OrderResponse> getOrderById(Long orderId);
    
    /**
     * Retrieves orders by customer ID
     * Returns paginated list of orders for a specific customer
     * 
     * @param customerId the customer ID to search for
     * @param pageable the pagination parameters
     * @return Page of orders for the customer
     */
    Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Retrieves orders by status
     * Returns paginated list of orders with specific status
     * 
     * @param status the order status to search for
     * @param pageable the pagination parameters
     * @return Page of orders with the specified status
     */
    Page<OrderResponse> getOrdersByStatus(String status, Pageable pageable);
    
    /**
     * Updates order status
     * Handles order status updates and triggers downstream workflows
     * 
     * @param orderId the order ID to update
     * @param status the new status
     * @return OrderResponse containing updated order details
     * @throws IllegalArgumentException if status is invalid
     * @throws IllegalStateException if order cannot be updated
     */
    OrderResponse updateOrderStatus(Long orderId, String status);
    
    /**
     * Cancels an order
     * Handles order cancellation and triggers downstream workflows
     * 
     * @param orderId the order ID to cancel
     * @return OrderResponse containing updated order details
     * @throws IllegalStateException if order cannot be cancelled
     */
    OrderResponse cancelOrder(Long orderId);
    
    /**
     * Retrieves all orders with pagination
     * Returns paginated list of all orders
     * 
     * @param pageable the pagination parameters
     * @return Page of all orders
     */
    Page<OrderResponse> getAllOrders(Pageable pageable);
    
    /**
     * Retrieves orders by date range
     * Returns paginated list of orders within date range
     * 
     * @param startDate the start date for the range
     * @param endDate the end date for the range
     * @param pageable the pagination parameters
     * @return Page of orders within the date range
     */
    Page<OrderResponse> getOrdersByDateRange(String startDate, String endDate, Pageable pageable);
    
    /**
     * Retrieves orders by amount range
     * Returns paginated list of orders within amount range
     * 
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @param pageable the pagination parameters
     * @return Page of orders within the amount range
     */
    Page<OrderResponse> getOrdersByAmountRange(String minAmount, String maxAmount, Pageable pageable);
    
    /**
     * Gets order statistics
     * Returns order statistics for analysis
     * 
     * @param startDate the start date for statistics (optional)
     * @param endDate the end date for statistics (optional)
     * @return Object containing order statistics
     */
    Object getOrderStatistics(String startDate, String endDate);
    
    /**
     * Gets orders needing attention
     * Returns list of orders that need attention
     * 
     * @return List of orders needing attention
     */
    List<OrderResponse> getOrdersNeedingAttention();
}
