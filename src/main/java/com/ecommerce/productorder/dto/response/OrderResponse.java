package com.ecommerce.productorder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order response
 * 
 * Design Principles Applied:
 * - Data Transfer Object Pattern: Transfers data between layers
 * - Immutability: Uses Builder pattern for object creation
 * - Single Responsibility: Only handles order response data
 * - Encapsulation: All order response data encapsulated
 * - Read-Only: No setters, only getters for data transfer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerEmail;
    private String status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemResponse> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * DTO for order item information in order response
     * 
     * Design Principles Applied:
     * - Inner Class: Encapsulates order item data within order context
     * - Immutability: Uses Builder pattern for object creation
     * - Single Responsibility: Only handles order item response data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private ProductInfo product;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        
        /**
         * DTO for product information in order item
         * 
         * Design Principles Applied:
         * - Inner Class: Encapsulates product data within order item context
         * - Immutability: Uses Builder pattern for object creation
         * - Single Responsibility: Only handles product info data
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ProductInfo {
            private Long id;
            private String name;
            private String sku;
        }
    }
}
