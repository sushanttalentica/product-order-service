package com.ecommerce.productorder.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
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
    
    @Getter
    @Setter
    public static class OrderItemRequest {
        
        @NotNull(message = "Product ID is required")
        @Positive(message = "Product ID must be positive")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 100, message = "Quantity must not exceed 100")
        private Integer quantity;
        
        public OrderItemRequest() {}
        
        public OrderItemRequest(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
    
    public CreateOrderRequest() {}
    
    public CreateOrderRequest(Long customerId, String customerEmail, String shippingAddress, 
                             List<OrderItemRequest> orderItems) {
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.shippingAddress = shippingAddress;
        this.orderItems = orderItems;
    }
}
