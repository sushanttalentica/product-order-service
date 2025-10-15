package com.ecommerce.productorder.domain.service.impl;

import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.domain.repository.CustomerRepository;
import com.ecommerce.productorder.domain.service.CustomerService;
import com.ecommerce.productorder.dto.request.CreateCustomerRequest;
import com.ecommerce.productorder.dto.request.UpdateCustomerRequest;
import com.ecommerce.productorder.dto.response.CustomerResponse;
import com.ecommerce.productorder.exception.BusinessException;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import com.ecommerce.productorder.mapper.CustomerMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;
  private final CustomerMapper customerMapper;
  private final PasswordEncoder passwordEncoder;

  public CustomerServiceImpl(
      CustomerRepository customerRepository,
      CustomerMapper customerMapper,
      PasswordEncoder passwordEncoder) {
    this.customerRepository = customerRepository;
    this.customerMapper = customerMapper;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public CustomerResponse createCustomer(CreateCustomerRequest request) {
    log.info("Creating customer with username: {}", request.getUsername());

    // Validate unique constraints
    if (customerRepository.existsByUsername(request.getUsername())) {
      throw new BusinessException("Username already exists: " + request.getUsername());
    }

    if (customerRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException("Email already exists: " + request.getEmail());
    }

    // Convert request to entity
    Customer customer = customerMapper.toEntity(request);

    // Encrypt password
    customer.setPassword(passwordEncoder.encode(request.getPassword()));

    // Save customer
    Customer savedCustomer = customerRepository.save(customer);
    log.info("Customer created successfully with ID: {}", savedCustomer.getId());

    return customerMapper.toResponse(savedCustomer);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CustomerResponse> getCustomerById(Long id) {
    log.debug("Retrieving customer with ID: {}", id);
    return customerRepository.findById(id).map(customerMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CustomerResponse> getCustomerByUsername(String username) {
    log.debug("Retrieving customer with username: {}", username);
    return customerRepository.findByUsername(username).map(customerMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<CustomerResponse> getCustomerByEmail(String email) {
    log.debug("Retrieving customer with email: {}", email);
    return customerRepository.findByEmail(email).map(customerMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
    log.debug("Retrieving all customers with pagination: {}", pageable);
    return customerRepository.findAll(pageable).map(customerMapper::toResponse);
  }

  @Override
  public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
    log.info("Updating customer with ID: {}", id);

    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

    // Validate email uniqueness if email is being updated
    if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
      if (customerRepository.existsByEmail(request.getEmail())) {
        throw new BusinessException("Email already exists: " + request.getEmail());
      }
    }

    Customer updatedCustomer = customerMapper.createUpdatedCustomer(customer, request);

    if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
      updatedCustomer.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    updatedCustomer = customerRepository.save(updatedCustomer);
    log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());

    return customerMapper.toResponse(updatedCustomer);
  }

  @Override
  public void deleteCustomer(Long id) {
    log.info("Deleting customer with ID: {}", id);

    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

    customerRepository.delete(customer);
    log.info("Customer deleted successfully with ID: {}", id);
  }

  @Override
  public CustomerResponse activateCustomer(Long id) {
    log.info("Activating customer with ID: {}", id);

    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

    customer.setActive(true);
    Customer updatedCustomer = customerRepository.save(customer);
    log.info("Customer activated successfully with ID: {}", id);

    return customerMapper.toResponse(updatedCustomer);
  }

  @Override
  public CustomerResponse deactivateCustomer(Long id) {
    log.info("Deactivating customer with ID: {}", id);

    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

    customer.setActive(false);
    Customer updatedCustomer = customerRepository.save(customer);
    log.info("Customer deactivated successfully with ID: {}", id);

    return customerMapper.toResponse(updatedCustomer);
  }

  @Override
  public CustomerResponse verifyCustomerEmail(Long id) {
    log.info("Verifying email for customer with ID: {}", id);

    Customer customer =
        customerRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));

    customer.setEmailVerified(true);
    Customer updatedCustomer = customerRepository.save(customer);
    log.info("Customer email verified successfully with ID: {}", id);

    return customerMapper.toResponse(updatedCustomer);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CustomerResponse> searchCustomers(
      String name, String email, Customer.CustomerRole role, Boolean isActive, Pageable pageable) {
    log.debug(
        "Searching customers with criteria - name: {}, email: {}, role: {}, isActive: {}",
        name,
        email,
        role,
        isActive);

    return customerRepository
        .searchCustomers(name, email, role, isActive, pageable)
        .map(customerMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomerResponse> getCustomersByRole(Customer.CustomerRole role) {
    log.debug("Retrieving customers by role: {}", role);
    return customerRepository.findByRole(role).stream()
        .map(customerMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomerResponse> getActiveCustomers() {
    log.debug("Retrieving active customers");
    return customerRepository.findByActiveTrue().stream()
        .map(customerMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomerResponse> getCustomersByCity(String city) {
    log.debug("Retrieving customers by city: {}", city);
    return customerRepository.findByCity(city).stream()
        .map(customerMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomerResponse> getCustomersByCountry(String country) {
    log.debug("Retrieving customers by country: {}", country);
    return customerRepository.findByCountry(country).stream()
        .map(customerMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CustomerResponse> getTopCustomersByOrderCount(Pageable pageable) {
    log.debug("Retrieving top customers by order count");
    return customerRepository
        .findTopCustomersByOrderCount(pageable)
        .map(customerMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public List<CustomerResponse> getCustomersWithNoOrders() {
    log.debug("Retrieving customers with no orders");
    return customerRepository.findCustomersWithNoOrders().stream()
        .map(customerMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean usernameExists(String username) {
    return customerRepository.existsByUsername(username);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean emailExists(String email) {
    return customerRepository.existsByEmail(email);
  }

  @Override
  @Transactional(readOnly = true)
  public long getCustomerCountByRole(Customer.CustomerRole role) {
    return customerRepository.countByRole(role);
  }

  @Override
  @Transactional(readOnly = true)
  public long getActiveCustomerCount() {
    return customerRepository.countByActiveTrue();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Customer> getCustomerEntityByUsername(String username) {
    log.debug("Retrieving customer entity with username: {}", username);
    return customerRepository.findByUsername(username);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Customer> getCustomerEntityByEmail(String email) {
    log.debug("Retrieving customer entity with email: {}", email);
    return customerRepository.findByEmail(email);
  }
}
