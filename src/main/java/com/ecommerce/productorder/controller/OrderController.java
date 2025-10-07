package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.domain.service.OrderService;
import com.ecommerce.productorder.dto.request.CreateOrderRequest;
import com.ecommerce.productorder.dto.response.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Order operations
 * 
 * Design Principles Applied:
 * - RESTful Design: Follows REST conventions for HTTP methods and status codes
 * - Single Responsibility: Only handles Order HTTP operations
 * - Dependency Injection: Uses constructor injection for dependencies
 * - Security: Uses Spring Security annotations for authorization
 * - Validation: Uses Bean Validation for input validation
 * - Error Handling: Delegates to global exception handler
 * - Logging: Uses SLF4J for logging
 * - Pagination: Supports pagination for list operations
 * - Command Query Separation: Separates read and write operations
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * Creates a new order
     * 
     * Design Principles Applied:
     * - POST for creation: Follows REST conventions
     * - Security: Requires CUSTOMER role
     * - Validation: Validates request body
     * - Response: Returns created order with 201 status
     * - Workflow Trigger: Triggers downstream workflows
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for customer ID: {}", request.getCustomerId());
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Retrieves an order by ID
     * 
     * Design Principles Applied:
     * - GET for retrieval: Follows REST conventions
     * - Security: Requires CUSTOMER role
     * - Path Variable: Uses path variable for resource identification
     * - Optional Response: Handles case when order not found
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        log.debug("Retrieving order with id: {}", orderId);
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Retrieves orders by customer ID
     * 
     * Design Principles Applied:
     * - GET for retrieval: Follows REST conventions
     * - Security: Requires CUSTOMER role
     * - Path Variable: Uses path variable for customer identification
     * - Pagination: Supports pagination for results
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByCustomerId(@PathVariable Long customerId,
                                                                    @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving orders for customer ID: {}", customerId);
        Page<OrderResponse> response = orderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves orders by status
     * 
     * Design Principles Applied:
     * - GET for retrieval: Follows REST conventions
     * - Security: Requires ADMIN role
     * - Query Parameter: Uses query parameter for status filtering
     * - Pagination: Supports pagination for results
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(@PathVariable String status,
                                                                @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving orders by status: {}", status);
        Page<OrderResponse> response = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates order status
     * 
     * Design Principles Applied:
     * - PATCH for partial updates: Follows REST conventions
     * - Security: Requires ADMIN role
     * - Path Variable: Uses path variable for resource identification
     * - Query Parameter: Uses query parameter for new status
     * - Workflow Trigger: Triggers downstream workflows
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId,
                                                          @RequestParam String status) {
        log.info("Updating order status for order ID: {} to {}", orderId, status);
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancels an order
     * 
     * Design Principles Applied:
     * - DELETE for cancellation: Follows REST conventions
     * - Security: Requires CUSTOMER or ADMIN role
     * - Path Variable: Uses path variable for resource identification
     * - Workflow Trigger: Triggers downstream workflows
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        log.info("Cancelling order with id: {}", orderId);
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves all orders with pagination
     * 
     * Design Principles Applied:
     * - GET for retrieval: Follows REST conventions
     * - Security: Requires ADMIN role
     * - Pagination: Supports pagination parameters
     * - Pageable: Uses Spring's Pageable for pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving all orders with pagination: {}", pageable);
        Page<OrderResponse> response = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves orders by date range
     * 
     * Design Principles Applied:
     * - GET for retrieval: Follows REST conventions
     * - Security: Requires ADMIN role
     * - Query Parameters: Uses query parameters for date range
     * - Pagination: Supports pagination for results
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByDateRange(@RequestParam String startDate,
                                                                   @RequestParam String endDate,
                                                                   @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving orders by date range: {} to {}", startDate, endDate);
        Page<OrderResponse> response = orderService.getOrdersByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves orders by amount range
     * 
     * Design Principles Applied:
     * - GET for retrieval: Follows REST conventions
     * - Security: Requires ADMIN role
     * - Query Parameters: Uses query parameters for amount range
     * - Pagination: Supports pagination for results
     */
    @GetMapping("/amount-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByAmountRange(@RequestParam String minAmount,
                                                                      @RequestParam String maxAmount,
                                                                      @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving orders by amount range: {} to {}", minAmount, maxAmount);
        Page<OrderResponse> response = orderService.getOrdersByAmountRange(minAmount, maxAmount, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets order statistics
     * 
     * Design Principles Applied:
     * - GET for retrieval: Follows REST conventions
     * - Security: Requires ADMIN role
     * - Query Parameter: Uses query parameter for date range
     * - Statistics: Returns order statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getOrderStatistics(@RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate) {
        log.debug("Retrieving order statistics for date range: {} to {}", startDate, endDate);
        Object statistics = orderService.getOrderStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Gets orders needing attention
     * 
     * Design Principles Applied:
     * - GET for retrieval: Follows REST conventions
     * - Security: Requires ADMIN role
     * - Business Logic: Returns orders that need attention
     * - List Response: Returns list of orders
     */
    @GetMapping("/needing-attention")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersNeedingAttention() {
        log.debug("Retrieving orders needing attention");
        List<OrderResponse> response = orderService.getOrdersNeedingAttention();
        return ResponseEntity.ok(response);
    }
}
