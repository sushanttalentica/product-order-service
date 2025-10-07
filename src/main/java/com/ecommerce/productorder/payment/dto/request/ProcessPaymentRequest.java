package com.ecommerce.productorder.payment.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

 

/**
 * Request DTO for processing payment
 * 
 * Design Principles Applied:
 * - Data Transfer Object: Encapsulates payment processing request data
 * - Validation: Uses Bean Validation annotations for input validation
 * - Immutability: Uses Builder pattern for object creation
 * - Single Responsibility: Only handles payment processing request data
 * - Encapsulation: All payment processing data encapsulated
 * - Value Objects: Uses BigDecimal for monetary precision
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequest {
    
    @NotNull(message = "Order ID is required")
    @Positive(message = "Order ID must be positive")
    private Long orderId;
    
    @NotNull(message = "Customer ID is required")
    @Positive(message = "Customer ID must be positive")
    private Long customerId;
    
    // Amount is derived from the Order total on server side
    
    @NotNull(message = "Payment method is required")
    private String paymentMethod;
    
    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be 13-19 digits")
    private String cardNumber;
    
    @NotBlank(message = "Card holder name is required")
    @Size(max = 100, message = "Card holder name must not exceed 100 characters")
    private String cardHolderName;
    
    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Expiry date must be in MM/YY format")
    private String expiryDate;
    
    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3-4 digits")
    private String cvv;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String customerEmail;
    
    @Size(max = 200, message = "Billing address must not exceed 200 characters")
    private String billingAddress;
    
    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;
    
    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;
    
    @Pattern(regexp = "^[0-9]{5,10}$", message = "Postal code must be 5-10 digits")
    private String postalCode;
    
    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;
}
