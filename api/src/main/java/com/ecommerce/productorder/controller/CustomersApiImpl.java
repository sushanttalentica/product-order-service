package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.api.CustomersApi;
import com.ecommerce.productorder.api.model.*;
import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.domain.service.CustomerService;
import com.ecommerce.productorder.dto.request.CreateCustomerRequest;
import com.ecommerce.productorder.dto.request.UpdateCustomerRequest;
import com.ecommerce.productorder.dto.response.CustomerResponse;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class CustomersApiImpl implements CustomersApi {

  private final CustomerService customerService;

  public CustomersApiImpl(CustomerService customerService) {
    this.customerService = customerService;
  }

  @Override
  public ResponseEntity<CustomerResponseApi> createCustomer(RegisterRequest registerRequest) {
    log.info("Creating customer: {}", registerRequest.getUsername());

    CreateCustomerRequest.AddressDto addressDto = new CreateCustomerRequest.AddressDto();
    addressDto.setStreetAddress(registerRequest.getStreetAddress());
    addressDto.setCity(registerRequest.getCity());
    addressDto.setState(registerRequest.getState());
    addressDto.setPostalCode(registerRequest.getPostalCode());
    addressDto.setCountry(registerRequest.getCountry());

    CreateCustomerRequest createRequest = new CreateCustomerRequest();
    createRequest.setUsername(registerRequest.getUsername());
    createRequest.setPassword(registerRequest.getPassword());
    createRequest.setEmail(registerRequest.getEmail());
    createRequest.setFirstName(registerRequest.getFirstName());
    createRequest.setLastName(registerRequest.getLastName());
    createRequest.setPhoneNumber(registerRequest.getPhoneNumber());
    createRequest.setAddress(addressDto);
    createRequest.setRole(Customer.CustomerRole.CUSTOMER);

    var response = customerService.createCustomer(createRequest);
    return ResponseEntity.status(201).body(convertToApiModel(response));
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Object> getAllCustomers(Integer page, Integer size) {
    Page<CustomerResponse> customersPage =
        customerService.getAllCustomers(PageRequest.of(page, size));
    return ResponseEntity.ok(customersPage.map(this::convertToApiModel));
  }

  @Override
  public ResponseEntity<CustomerResponseApi> getCustomerById(Long id) {
    return customerService
        .getCustomerById(id)
        .map(this::convertToApiModel)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.status(404).body(null));
  }

  @Override
  public ResponseEntity<CustomerResponseApi> updateCustomer(
      Long id, UpdateCustomerRequestApi updateCustomerRequest) {
    log.info("Updating customer: {}", id);

    UpdateCustomerRequest.AddressDto addressDto = new UpdateCustomerRequest.AddressDto();
    addressDto.setStreetAddress(updateCustomerRequest.getStreetAddress());
    addressDto.setCity(updateCustomerRequest.getCity());
    addressDto.setState(updateCustomerRequest.getState());
    addressDto.setPostalCode(updateCustomerRequest.getPostalCode());
    addressDto.setCountry(updateCustomerRequest.getCountry());

    UpdateCustomerRequest dtoRequest = new UpdateCustomerRequest();
    dtoRequest.setFirstName(updateCustomerRequest.getFirstName());
    dtoRequest.setLastName(updateCustomerRequest.getLastName());
    dtoRequest.setPhoneNumber(updateCustomerRequest.getPhoneNumber());
    dtoRequest.setAddress(addressDto);

    var response = customerService.updateCustomer(id, dtoRequest);
    return ResponseEntity.ok(convertToApiModel(response));
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<MessageResponse> deleteCustomer(Long id) {
    log.info("Deleting customer: {}", id);
    customerService.deleteCustomer(id);
    return ResponseEntity.ok(
        new MessageResponse().message("Customer deleted successfully").success(true));
  }

  @Override
  public ResponseEntity<CustomerResponseApi> getCustomerByUsername(String username) {
    return customerService
        .getCustomerByUsername(username)
        .map(this::convertToApiModel)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.status(404).body(null));
  }

  @Override
  public ResponseEntity<CustomerResponseApi> activateCustomer(Long id) {
    log.info("Activating customer: {}", id);
    var response = customerService.activateCustomer(id);
    return ResponseEntity.ok(convertToApiModel(response));
  }

  @Override
  public ResponseEntity<CustomerResponseApi> deactivateCustomer(Long id) {
    log.info("Deactivating customer: {}", id);
    var response = customerService.deactivateCustomer(id);
    return ResponseEntity.ok(convertToApiModel(response));
  }

  @Override
  public ResponseEntity<CustomerResponseApi> verifyCustomerEmail(Long id) {
    log.info("Verifying email for customer: {}", id);
    var response = customerService.verifyCustomerEmail(id);
    return ResponseEntity.ok(convertToApiModel(response));
  }

  @Override
  public ResponseEntity<Object> searchCustomers(String keyword, Integer page, Integer size) {
    Page<CustomerResponse> customersPage =
        customerService.searchCustomers(keyword, keyword, null, null, PageRequest.of(page, size));
    return ResponseEntity.ok(customersPage);
  }

  @Override
  public ResponseEntity<Object> getCustomersByRole(String role, Integer page, Integer size) {
    List<CustomerResponse> customers =
        customerService.getCustomersByRole(Customer.CustomerRole.valueOf(role));
    return ResponseEntity.ok(customers);
  }

  @Override
  public ResponseEntity<Object> getActiveCustomers(Integer page, Integer size) {
    List<CustomerResponse> customers = customerService.getActiveCustomers();
    return ResponseEntity.ok(customers);
  }

  @Override
  public ResponseEntity<Object> getCustomersByCity(String city, Integer page, Integer size) {
    List<CustomerResponse> customers = customerService.getCustomersByCity(city);
    return ResponseEntity.ok(customers);
  }

  @Override
  public ResponseEntity<List<CustomerResponseApi>> getTopCustomers(Integer limit) {
    var customers = customerService.getTopCustomersByOrderCount(PageRequest.of(0, limit));
    return ResponseEntity.ok(
        customers.stream().map(this::convertToApiModel).collect(Collectors.toList()));
  }

  @Override
  public ResponseEntity<CustomerStatistics> getCustomerStatistics() {
    var apiStats = new CustomerStatistics();
    apiStats.setTotalCustomers(0L);
    apiStats.setActiveCustomers(0L);
    apiStats.setVerifiedEmails(0L);
    return ResponseEntity.ok(apiStats);
  }

  private CustomerResponseApi convertToApiModel(CustomerResponse dto) {
    var apiModel = new CustomerResponseApi();
    apiModel.setId(dto.id());
    apiModel.setUsername(dto.username());
    apiModel.setEmail(dto.email());
    apiModel.setFirstName(dto.firstName());
    apiModel.setLastName(dto.lastName());
    apiModel.setFullName(dto.fullName());
    apiModel.setPhoneNumber(dto.phoneNumber());
    apiModel.setRole(dto.role() != null ? dto.role().name() : null);
    apiModel.setIsActive(dto.active());
    apiModel.setEmailVerified(dto.emailVerified());
    apiModel.setCreatedAt(
        dto.createdAt() != null ? dto.createdAt().atOffset(ZoneOffset.UTC) : null);
    apiModel.setUpdatedAt(
        dto.updatedAt() != null ? dto.updatedAt().atOffset(ZoneOffset.UTC) : null);
    apiModel.setOrderCount(dto.orderCount());

    if (dto.address() != null) {
      var address = new AddressResponseApi();
      address.setStreetAddress(dto.address().streetAddress());
      address.setCity(dto.address().city());
      address.setState(dto.address().state());
      address.setPostalCode(dto.address().postalCode());
      address.setCountry(dto.address().country());
      apiModel.setAddress(address);
    }

    return apiModel;
  }
}
