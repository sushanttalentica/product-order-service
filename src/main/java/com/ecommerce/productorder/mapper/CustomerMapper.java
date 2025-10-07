package com.ecommerce.productorder.mapper;

import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.dto.request.CreateCustomerRequest;
import com.ecommerce.productorder.dto.request.UpdateCustomerRequest;
import com.ecommerce.productorder.dto.response.CustomerResponse;
import org.springframework.stereotype.Component;

/**
 * Customer Mapper
 * 
 * Mapper for converting between Customer entities and DTOs.
 * 
 * Design Principles Applied:
 * - Mapper Pattern: Separates entity-DTO conversion logic
 * - Single Responsibility: Focuses only on mapping operations
 * - Null Safety: Handles null values gracefully
 * - Immutability: Creates new objects rather than modifying existing ones
 */
@Component
public class CustomerMapper {

    /**
     * Convert CreateCustomerRequest to Customer entity
     */
    public Customer toEntity(CreateCustomerRequest request) {
        if (request == null) {
            return null;
        }

        Customer.Address address = null;
        if (request.getAddress() != null) {
            address = Customer.Address.builder()
                    .streetAddress(request.getAddress().getStreetAddress())
                    .city(request.getAddress().getCity())
                    .state(request.getAddress().getState())
                    .postalCode(request.getAddress().getPostalCode())
                    .country(request.getAddress().getCountry())
                    .build();
        }

        return Customer.builder()
                .username(request.getUsername())
                .password(request.getPassword()) // Note: Should be encrypted in service layer
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .address(address)
                .role(request.getRole())
                .isActive(true)
                .emailVerified(false)
                .build();
    }

    /**
     * Convert Customer entity to CustomerResponse
     */
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerResponse.AddressResponse addressResponse = null;
        if (customer.getAddress() != null) {
            addressResponse = CustomerResponse.AddressResponse.builder()
                    .streetAddress(customer.getAddress().getStreetAddress())
                    .city(customer.getAddress().getCity())
                    .state(customer.getAddress().getState())
                    .postalCode(customer.getAddress().getPostalCode())
                    .country(customer.getAddress().getCountry())
                    .build();
        }

        return CustomerResponse.builder()
                .id(customer.getId())
                .username(customer.getUsername())
                .email(customer.getEmail())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(customer.getFullName())
                .phoneNumber(customer.getPhoneNumber())
                .address(addressResponse)
                .role(customer.getRole())
                .isActive(customer.getIsActive())
                .emailVerified(customer.getEmailVerified())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .orderCount(customer.getOrders() != null ? (long) customer.getOrders().size() : 0L)
                .build();
    }

    /**
     * Update existing Customer entity with UpdateCustomerRequest
     */
    public void updateEntity(Customer customer, UpdateCustomerRequest request) {
        if (customer == null || request == null) {
            return;
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            customer.setPassword(request.getPassword()); // Note: Should be encrypted in service layer
        }

        // Update email if provided
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            customer.setEmail(request.getEmail());
        }

        // Update first name if provided
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            customer.setFirstName(request.getFirstName());
        }

        // Update last name if provided
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            customer.setLastName(request.getLastName());
        }

        // Update phone number if provided
        if (request.getPhoneNumber() != null) {
            customer.setPhoneNumber(request.getPhoneNumber());
        }

        // Update address if provided
        if (request.getAddress() != null) {
            Customer.Address address = customer.getAddress();
            if (address == null) {
                address = new Customer.Address();
                customer.setAddress(address);
            }

            if (request.getAddress().getStreetAddress() != null) {
                address.setStreetAddress(request.getAddress().getStreetAddress());
            }
            if (request.getAddress().getCity() != null) {
                address.setCity(request.getAddress().getCity());
            }
            if (request.getAddress().getState() != null) {
                address.setState(request.getAddress().getState());
            }
            if (request.getAddress().getPostalCode() != null) {
                address.setPostalCode(request.getAddress().getPostalCode());
            }
            if (request.getAddress().getCountry() != null) {
                address.setCountry(request.getAddress().getCountry());
            }
        }

        // Update active status if provided
        if (request.getIsActive() != null) {
            customer.setIsActive(request.getIsActive());
        }

        // Update email verified status if provided
        if (request.getEmailVerified() != null) {
            customer.setEmailVerified(request.getEmailVerified());
        }
    }

    /**
     * Convert CreateCustomerRequest.AddressDto to Customer.Address
     */
    public Customer.Address toAddressEntity(CreateCustomerRequest.AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        return Customer.Address.builder()
                .streetAddress(addressDto.getStreetAddress())
                .city(addressDto.getCity())
                .state(addressDto.getState())
                .postalCode(addressDto.getPostalCode())
                .country(addressDto.getCountry())
                .build();
    }

    /**
     * Convert UpdateCustomerRequest.AddressDto to Customer.Address
     */
    public Customer.Address toAddressEntity(UpdateCustomerRequest.AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }

        return Customer.Address.builder()
                .streetAddress(addressDto.getStreetAddress())
                .city(addressDto.getCity())
                .state(addressDto.getState())
                .postalCode(addressDto.getPostalCode())
                .country(addressDto.getCountry())
                .build();
    }

    /**
     * Convert Customer.Address to CustomerResponse.AddressResponse
     */
    public CustomerResponse.AddressResponse toAddressResponse(Customer.Address address) {
        if (address == null) {
            return null;
        }

        return CustomerResponse.AddressResponse.builder()
                .streetAddress(address.getStreetAddress())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }
}
