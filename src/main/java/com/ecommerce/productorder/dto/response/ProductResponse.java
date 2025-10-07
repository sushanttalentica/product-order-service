package com.ecommerce.productorder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for product response
 * 
 * Design Principles Applied:
 * - Data Transfer Object Pattern: Transfers data between layers
 * - Immutability: Uses Builder pattern for object creation
 * - Single Responsibility: Only handles product response data
 * - Encapsulation: All product response data encapsulated
 * - Read-Only: No setters, only getters for data transfer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String sku;
    private Boolean isActive;
    private CategoryResponse category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * DTO for category information in product response
     * 
     * Design Principles Applied:
     * - Inner Class: Encapsulates category data within product context
     * - Immutability: Uses Builder pattern for object creation
     * - Single Responsibility: Only handles category response data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
        private Boolean isActive;
    }
}
