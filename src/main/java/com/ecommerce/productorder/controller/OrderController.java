package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.domain.service.OrderService;
import com.ecommerce.productorder.dto.request.CreateOrderRequest;
import com.ecommerce.productorder.dto.response.MessageResponse;
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

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    
    private final OrderService orderService;
    
    
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("Creating order for customer ID: {}", request.getCustomerId());
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        log.debug("Retrieving order with id: {}", orderId);
        return orderService.getOrderById(orderId)
                .map(order -> ResponseEntity.ok((Object) order))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(MessageResponse.error("Order not found with ID: " + orderId)));
    }
    
    
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByCustomerId(@PathVariable Long customerId,
                                                                    @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving orders for customer ID: {}", customerId);
        Page<OrderResponse> response = orderService.getOrdersByCustomerId(customerId, pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(@PathVariable String status,
                                                                @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving orders by status: {}", status);
        Page<OrderResponse> response = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId,
                                                          @RequestParam String status) {
        log.info("Updating order status for order ID: {} to {}", orderId, status);
        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(response);
    }
    
    
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        log.info("Cancelling order with id: {}", orderId);
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving all orders with pagination: {}", pageable);
        Page<OrderResponse> response = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByDateRange(@RequestParam String startDate,
                                                                   @RequestParam String endDate,
                                                                   @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving orders by date range: {} to {}", startDate, endDate);
        Page<OrderResponse> response = orderService.getOrdersByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/amount-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByAmountRange(@RequestParam String minAmount,
                                                                      @RequestParam String maxAmount,
                                                                      @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving orders by amount range: {} to {}", minAmount, maxAmount);
        Page<OrderResponse> response = orderService.getOrdersByAmountRange(minAmount, maxAmount, pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getOrderStatistics(@RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate) {
        log.debug("Retrieving order statistics for date range: {} to {}", startDate, endDate);
        Object statistics = orderService.getOrderStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    
    @GetMapping("/needing-attention")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersNeedingAttention() {
        log.debug("Retrieving orders needing attention");
        List<OrderResponse> response = orderService.getOrdersNeedingAttention();
        return ResponseEntity.ok(response);
    }
}
