package com.ecommerce.productorder.mapper;

import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.dto.request.CreateCustomerRequest;
import com.ecommerce.productorder.dto.request.UpdateCustomerRequest;
import com.ecommerce.productorder.dto.response.CustomerResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

     // Convert CreateCustomerRequest to Customer entity
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

     // Convert Customer entity to CustomerResponse
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerResponse.AddressResponse addressResponse = null;
        if (customer.getAddress() != null) {
            addressResponse = new CustomerResponse.AddressResponse(
                    customer.getAddress().getStreetAddress(),
                    customer.getAddress().getCity(),
                    customer.getAddress().getState(),
                    customer.getAddress().getPostalCode(),
                    customer.getAddress().getCountry()
            );
        }

        return new CustomerResponse(
                customer.getId(),
                customer.getUsername(),
                customer.getEmail(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getFullName(),
                customer.getPhoneNumber(),
                addressResponse,
                customer.getRole(),
                customer.getIsActive(),
                customer.getEmailVerified(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getOrders() != null ? (long) customer.getOrders().size() : 0L
        );
    }

    public Customer createUpdatedCustomer(Customer existingCustomer, UpdateCustomerRequest request) {
        if (existingCustomer == null || request == null) {
            return existingCustomer;
        }

        Customer updatedCustomer = Customer.builder()
                .id(existingCustomer.getId())
                .username(existingCustomer.getUsername())
                .password(request.getPassword() != null && !request.getPassword().trim().isEmpty() 
                    ? request.getPassword() 
                    : existingCustomer.getPassword())
                .email(request.getEmail() != null && !request.getEmail().trim().isEmpty() 
                    ? request.getEmail() 
                    : existingCustomer.getEmail())
                .firstName(request.getFirstName() != null && !request.getFirstName().trim().isEmpty() 
                    ? request.getFirstName() 
                    : existingCustomer.getFirstName())
                .lastName(request.getLastName() != null && !request.getLastName().trim().isEmpty() 
                    ? request.getLastName() 
                    : existingCustomer.getLastName())
                .phoneNumber(request.getPhoneNumber() != null 
                    ? request.getPhoneNumber() 
                    : existingCustomer.getPhoneNumber())
                .address(request.getAddress() != null 
                    ? createUpdatedAddress(existingCustomer.getAddress(), request.getAddress()) 
                    : existingCustomer.getAddress())
                .role(existingCustomer.getRole())
                .isActive(request.getIsActive() != null 
                    ? request.getIsActive() 
                    : existingCustomer.getIsActive())
                .emailVerified(request.getEmailVerified() != null 
                    ? request.getEmailVerified() 
                    : existingCustomer.getEmailVerified())
                .createdAt(existingCustomer.getCreatedAt())
                .build();
        
        return updatedCustomer;
    }
    
    private Customer.Address createUpdatedAddress(Customer.Address existingAddress, UpdateCustomerRequest.AddressDto requestAddress) {
        Customer.Address baseAddress = existingAddress != null ? existingAddress : new Customer.Address();
        
        return Customer.Address.builder()
                .streetAddress(requestAddress.getStreetAddress() != null 
                    ? requestAddress.getStreetAddress() 
                    : baseAddress.getStreetAddress())
                .city(requestAddress.getCity() != null 
                    ? requestAddress.getCity() 
                    : baseAddress.getCity())
                .state(requestAddress.getState() != null 
                    ? requestAddress.getState() 
                    : baseAddress.getState())
                .postalCode(requestAddress.getPostalCode() != null 
                    ? requestAddress.getPostalCode() 
                    : baseAddress.getPostalCode())
                .country(requestAddress.getCountry() != null 
                    ? requestAddress.getCountry() 
                    : baseAddress.getCountry())
                .build();
    }

     // Convert CreateCustomerRequest.AddressDto to Customer.Address

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

     // Convert UpdateCustomerRequest.AddressDto to Customer.Address

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


     // Convert Customer.Address to CustomerResponse.AddressResponse

    public CustomerResponse.AddressResponse toAddressResponse(Customer.Address address) {
        if (address == null) {
            return null;
        }

        return new CustomerResponse.AddressResponse(
                address.getStreetAddress(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
    }
}
