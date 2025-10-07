package com.ecommerce.productorder.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating a new product
 * 
 * Design Principles Applied:
 * - Data Transfer Object Pattern: Transfers data between layers
 * - Validation: Uses Bean Validation annotations for input validation
 * - Immutability: Uses Builder pattern for object creation
 * - Single Responsibility: Only handles product creation data
 * - Encapsulation: All product creation data encapsulated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be non-negative")
    private Integer stockQuantity;
    
    @NotBlank(message = "SKU is required")
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9-_]+$", message = "SKU must contain only uppercase letters, numbers, hyphens, and underscores")
    private String sku;
    
    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;
}
