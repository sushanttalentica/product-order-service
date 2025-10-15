package com.ecommerce.productorder.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateProductRequest {
    
    @Size(max = 200, message = "Product name must not exceed 200 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock quantity must be non-negative")
    private Integer stockQuantity;
    
    @Size(max = 50, message = "SKU must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9-_]+$", message = "SKU must contain only uppercase letters, numbers, hyphens, and underscores")
    private String sku;
    
    @Positive(message = "Category ID must be positive")
    private Long categoryId;
    
    private Boolean isActive;
    
    public UpdateProductRequest() {}
    
    public UpdateProductRequest(String name, String description, BigDecimal price, 
                                Integer stockQuantity, String sku, Long categoryId, Boolean isActive) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.sku = sku;
        this.categoryId = categoryId;
        this.isActive = isActive;
    }
}
