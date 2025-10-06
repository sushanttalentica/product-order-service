package com.ecommerce.productorder.dto.response;

import com.ecommerce.productorder.domain.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Customer Response DTO
 * 
 * DTO for returning customer information in API responses.
 * 
 * Design Principles Applied:
 * - Data Transfer Object: Separates API contract from entity
 * - Security: Excludes sensitive information like password
 * - Builder Pattern: Uses Lombok builder for object creation
 * - Read-only: Immutable response object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private AddressResponse address;
    private Customer.CustomerRole role;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long orderCount;

    /**
     * Address Response DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private String streetAddress;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
