package com.ecommerce.productorder.dto.request;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Getter
@Setter
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

    private Boolean active;

    private Boolean emailVerified;

    @Getter
    @Setter
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
        
        public AddressDto() {}
        
        public AddressDto(String streetAddress, String city, String state, String postalCode, String country) {
            this.streetAddress = streetAddress;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.country = country;
        }
    }
    
    public UpdateCustomerRequest() {}
    
    public UpdateCustomerRequest(String password, String email, String firstName, String lastName,
                                 String phoneNumber, AddressDto address, Boolean active, Boolean emailVerified) {
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.active = active;
        this.emailVerified = emailVerified;
    }
}
