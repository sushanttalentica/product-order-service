package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.api.CustomersApi;
import com.ecommerce.productorder.api.model.*;
import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.domain.service.CustomerService;
import com.ecommerce.productorder.dto.request.CreateCustomerRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class CustomersApiImpl implements CustomersApi {

    private final CustomerService customerService;    
    public CustomersApiImpl(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public ResponseEntity<CustomerResponse> createCustomer(RegisterRequest registerRequest) {
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
    public ResponseEntity<Object> getAllCustomers(Integer page, Integer size) {
        Page<com.ecommerce.productorder.dto.response.CustomerResponse> customersPage = 
                customerService.getAllCustomers(PageRequest.of(page, size));
        return ResponseEntity.ok(customersPage);
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomerById(Long id) {
        return customerService.getCustomerById(id)
                .map(this::convertToApiModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(null));
    }

    @Override
    public ResponseEntity<CustomerResponse> updateCustomer(
            Long id, com.ecommerce.productorder.api.model.UpdateCustomerRequest updateCustomerRequest) {
        log.info("Updating customer: {}", id);
        
        com.ecommerce.productorder.dto.request.UpdateCustomerRequest.AddressDto addressDto = 
                new com.ecommerce.productorder.dto.request.UpdateCustomerRequest.AddressDto();
        addressDto.setStreetAddress(updateCustomerRequest.getStreetAddress());
        addressDto.setCity(updateCustomerRequest.getCity());
        addressDto.setState(updateCustomerRequest.getState());
        addressDto.setPostalCode(updateCustomerRequest.getPostalCode());
        addressDto.setCountry(updateCustomerRequest.getCountry());
        
        com.ecommerce.productorder.dto.request.UpdateCustomerRequest dtoRequest = 
                new com.ecommerce.productorder.dto.request.UpdateCustomerRequest();
        dtoRequest.setFirstName(updateCustomerRequest.getFirstName());
        dtoRequest.setLastName(updateCustomerRequest.getLastName());
        dtoRequest.setPhoneNumber(updateCustomerRequest.getPhoneNumber());
        dtoRequest.setAddress(addressDto);
        
        var response = customerService.updateCustomer(id, dtoRequest);
        return ResponseEntity.ok(convertToApiModel(response));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteCustomer(Long id) {
        log.info("Deleting customer: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(new MessageResponse()
                .message("Customer deleted successfully")
                .success(true));
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomerByUsername(String username) {
        return customerService.getCustomerByUsername(username)
                .map(this::convertToApiModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(null));
    }

    @Override
    public ResponseEntity<CustomerResponse> activateCustomer(Long id) {
        log.info("Activating customer: {}", id);
        var response = customerService.activateCustomer(id);
        return ResponseEntity.ok(convertToApiModel(response));
    }

    @Override
    public ResponseEntity<CustomerResponse> deactivateCustomer(Long id) {
        log.info("Deactivating customer: {}", id);
        var response = customerService.deactivateCustomer(id);
        return ResponseEntity.ok(convertToApiModel(response));
    }

    @Override
    public ResponseEntity<CustomerResponse> verifyCustomerEmail(Long id) {
        log.info("Verifying email for customer: {}", id);
        var response = customerService.verifyCustomerEmail(id);
        return ResponseEntity.ok(convertToApiModel(response));
    }

    @Override
    public ResponseEntity<Object> searchCustomers(String keyword, Integer page, Integer size) {
        Page<com.ecommerce.productorder.dto.response.CustomerResponse> customersPage = 
                customerService.searchCustomers(keyword, keyword, null, null, PageRequest.of(page, size));
        return ResponseEntity.ok(customersPage);
    }

    @Override
    public ResponseEntity<Object> getCustomersByRole(String role, Integer page, Integer size) {
        List<com.ecommerce.productorder.dto.response.CustomerResponse> customers = 
                customerService.getCustomersByRole(Customer.CustomerRole.valueOf(role));
        return ResponseEntity.ok(customers);
    }

    @Override
    public ResponseEntity<Object> getActiveCustomers(Integer page, Integer size) {
        List<com.ecommerce.productorder.dto.response.CustomerResponse> customers = 
                customerService.getActiveCustomers();
        return ResponseEntity.ok(customers);
    }

    @Override
    public ResponseEntity<Object> getCustomersByCity(String city, Integer page, Integer size) {
        List<com.ecommerce.productorder.dto.response.CustomerResponse> customers = 
                customerService.getCustomersByCity(city);
        return ResponseEntity.ok(customers);
    }

    @Override
    public ResponseEntity<List<com.ecommerce.productorder.api.model.CustomerResponse>> getTopCustomers(Integer limit) {
        var customers = customerService.getTopCustomersByOrderCount(PageRequest.of(0, limit));
        return ResponseEntity.ok(customers.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<CustomerStatistics> getCustomerStatistics() {
        var apiStats = new CustomerStatistics();
        apiStats.setTotalCustomers(0L);
        apiStats.setActiveCustomers(0L);
        apiStats.setVerifiedEmails(0L);
        return ResponseEntity.ok(apiStats);
    }

    private com.ecommerce.productorder.api.model.CustomerResponse convertToApiModel(
            com.ecommerce.productorder.dto.response.CustomerResponse dto) {
        var apiModel = new com.ecommerce.productorder.api.model.CustomerResponse();
        apiModel.setId(dto.id());
        apiModel.setUsername(dto.username());
        apiModel.setEmail(dto.email());
        apiModel.setFirstName(dto.firstName());
        apiModel.setLastName(dto.lastName());
        apiModel.setFullName(dto.fullName());
        apiModel.setPhoneNumber(dto.phoneNumber());
        apiModel.setRole(dto.role() != null ? dto.role().name() : null);
        apiModel.setIsActive(dto.isActive());
        apiModel.setEmailVerified(dto.emailVerified());
        apiModel.setCreatedAt(dto.createdAt() != null ? dto.createdAt().atOffset(ZoneOffset.UTC) : null);
        apiModel.setUpdatedAt(dto.updatedAt() != null ? dto.updatedAt().atOffset(ZoneOffset.UTC) : null);
        apiModel.setOrderCount(dto.orderCount());
        
        if (dto.address() != null) {
            var address = new AddressResponse();
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

