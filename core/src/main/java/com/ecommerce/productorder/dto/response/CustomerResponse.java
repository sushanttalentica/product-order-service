package com.ecommerce.productorder.dto.response;

import com.ecommerce.productorder.domain.entity.Customer;

import java.time.LocalDateTime;

public record CustomerResponse(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    String fullName,
    String phoneNumber,
    AddressResponse address,
    Customer.CustomerRole role,
    Boolean active,
    Boolean emailVerified,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long orderCount
) {
    public record AddressResponse(
        String streetAddress,
        String city,
        String state,
        String postalCode,
        String country
    ) {}
}
