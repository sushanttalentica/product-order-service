package com.ecommerce.productorder.payment.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;

 

@Getter
@Setter
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
    
    public ProcessPaymentRequest() {}
    
    public ProcessPaymentRequest(Long orderId, Long customerId, String paymentMethod, String cardNumber,
                                String cardHolderName, String expiryDate, String cvv, String description,
                                String customerEmail, String billingAddress, String city, String state,
                                String postalCode, String country) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentMethod = paymentMethod;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.description = description;
        this.customerEmail = customerEmail;
        this.billingAddress = billingAddress;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }
}
