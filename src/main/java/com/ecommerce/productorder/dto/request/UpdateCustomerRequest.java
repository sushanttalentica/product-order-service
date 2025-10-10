package com.ecommerce.productorder.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phoneNumber;

    private AddressDto address;

    private Boolean isActive;

    private Boolean emailVerified;

    /**
     * Address DTO for nested validation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        
        @Size(max = 200, message = "Street address must not exceed 200 characters")
        private String streetAddress;

        @Size(max = 50, message = "City must not exceed 50 characters")
        private String city;

        @Size(max = 50, message = "State must not exceed 50 characters")
        private String state;

        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        private String postalCode;

        @Size(max = 50, message = "Country must not exceed 50 characters")
        private String country;
    }
}
