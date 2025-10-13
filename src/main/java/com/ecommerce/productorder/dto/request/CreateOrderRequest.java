package com.ecommerce.productorder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;
    
    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be valid")
    @Size(max = 100, message = "Customer email must not exceed 100 characters")
    private String customerEmail;
    
    @Size(max = 500, message = "Shipping address must not exceed 500 characters")
    private String shippingAddress;
    
    @NotEmpty(message = "Order items are required")
    @Valid
    private List<OrderItemRequest> orderItems;
    
    /**
     * Calculate total amount for the order
     * This method would typically calculate based on order items and product prices
     * For now, returns a placeholder value
     */
    public BigDecimal getTotalAmount() {
        // This is a placeholder implementation
        // In a real scenario, this would calculate based on order items and product prices
        return BigDecimal.ZERO;
    }
    
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        
        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID must be positive")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 100, message = "Quantity must not exceed 100")
        private Integer quantity;
    }
}
