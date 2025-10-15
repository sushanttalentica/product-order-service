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
      address = new Customer.Address();
      address.setStreetAddress(request.getAddress().getStreetAddress());
      address.setCity(request.getAddress().getCity());
      address.setState(request.getAddress().getState());
      address.setPostalCode(request.getAddress().getPostalCode());
      address.setCountry(request.getAddress().getCountry());
    }

    Customer customer = new Customer();
    customer.setUsername(request.getUsername());
    customer.setPassword(request.getPassword());
    customer.setEmail(request.getEmail());
    customer.setFirstName(request.getFirstName());
    customer.setLastName(request.getLastName());
    customer.setPhoneNumber(request.getPhoneNumber());
    customer.setAddress(address);
    customer.setRole(request.getRole());
    customer.setActive(true);
    customer.setEmailVerified(false);
    return customer;
  }

  // Convert Customer entity to CustomerResponse
  public CustomerResponse toResponse(Customer customer) {
    if (customer == null) {
      return null;
    }

    CustomerResponse.AddressResponse addressResponse = null;
    if (customer.getAddress() != null) {
      addressResponse =
          new CustomerResponse.AddressResponse(
              customer.getAddress().getStreetAddress(),
              customer.getAddress().getCity(),
              customer.getAddress().getState(),
              customer.getAddress().getPostalCode(),
              customer.getAddress().getCountry());
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
        customer.getActive(),
        customer.getEmailVerified(),
        customer.getCreatedAt(),
        customer.getUpdatedAt(),
        customer.getOrders() != null ? (long) customer.getOrders().size() : 0L);
  }

  public Customer createUpdatedCustomer(Customer existingCustomer, UpdateCustomerRequest request) {
    if (existingCustomer == null || request == null) {
      return existingCustomer;
    }

    Customer updatedCustomer = new Customer();
    updatedCustomer.setId(existingCustomer.getId());
    updatedCustomer.setUsername(existingCustomer.getUsername());
    updatedCustomer.setPassword(
        request.getPassword() != null && !request.getPassword().trim().isEmpty()
            ? request.getPassword()
            : existingCustomer.getPassword());
    updatedCustomer.setEmail(
        request.getEmail() != null && !request.getEmail().trim().isEmpty()
            ? request.getEmail()
            : existingCustomer.getEmail());
    updatedCustomer.setFirstName(
        request.getFirstName() != null && !request.getFirstName().trim().isEmpty()
            ? request.getFirstName()
            : existingCustomer.getFirstName());
    updatedCustomer.setLastName(
        request.getLastName() != null && !request.getLastName().trim().isEmpty()
            ? request.getLastName()
            : existingCustomer.getLastName());
    updatedCustomer.setPhoneNumber(
        request.getPhoneNumber() != null
            ? request.getPhoneNumber()
            : existingCustomer.getPhoneNumber());
    updatedCustomer.setAddress(
        request.getAddress() != null
            ? createUpdatedAddress(existingCustomer.getAddress(), request.getAddress())
            : existingCustomer.getAddress());
    updatedCustomer.setRole(existingCustomer.getRole());
    updatedCustomer.setActive(
        request.getActive() != null ? request.getActive() : existingCustomer.getActive());
    updatedCustomer.setEmailVerified(
        request.getEmailVerified() != null
            ? request.getEmailVerified()
            : existingCustomer.getEmailVerified());
    updatedCustomer.setCreatedAt(existingCustomer.getCreatedAt());

    return updatedCustomer;
  }

  private Customer.Address createUpdatedAddress(
      Customer.Address existingAddress, UpdateCustomerRequest.AddressDto requestAddress) {
    Customer.Address baseAddress =
        existingAddress != null ? existingAddress : new Customer.Address();

    Customer.Address address = new Customer.Address();
    address.setStreetAddress(
        requestAddress.getStreetAddress() != null
            ? requestAddress.getStreetAddress()
            : baseAddress.getStreetAddress());
    address.setCity(
        requestAddress.getCity() != null ? requestAddress.getCity() : baseAddress.getCity());
    address.setState(
        requestAddress.getState() != null ? requestAddress.getState() : baseAddress.getState());
    address.setPostalCode(
        requestAddress.getPostalCode() != null
            ? requestAddress.getPostalCode()
            : baseAddress.getPostalCode());
    address.setCountry(
        requestAddress.getCountry() != null
            ? requestAddress.getCountry()
            : baseAddress.getCountry());
    return address;
  }

  // Convert CreateCustomerRequest.AddressDto to Customer.Address

  public Customer.Address toAddressEntity(CreateCustomerRequest.AddressDto addressDto) {
    if (addressDto == null) {
      return null;
    }

    Customer.Address address = new Customer.Address();
    address.setStreetAddress(addressDto.getStreetAddress());
    address.setCity(addressDto.getCity());
    address.setState(addressDto.getState());
    address.setPostalCode(addressDto.getPostalCode());
    address.setCountry(addressDto.getCountry());
    return address;
  }

  // Convert UpdateCustomerRequest.AddressDto to Customer.Address

  public Customer.Address toAddressEntity(UpdateCustomerRequest.AddressDto addressDto) {
    if (addressDto == null) {
      return null;
    }

    Customer.Address address = new Customer.Address();
    address.setStreetAddress(addressDto.getStreetAddress());
    address.setCity(addressDto.getCity());
    address.setState(addressDto.getState());
    address.setPostalCode(addressDto.getPostalCode());
    address.setCountry(addressDto.getCountry());
    return address;
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
        address.getCountry());
  }
}
