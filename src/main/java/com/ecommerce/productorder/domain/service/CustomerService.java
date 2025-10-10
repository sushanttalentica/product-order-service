package com.ecommerce.productorder.domain.service;

import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.dto.request.CreateCustomerRequest;
import com.ecommerce.productorder.dto.request.UpdateCustomerRequest;
import com.ecommerce.productorder.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request);

    /**
     * Get customer by ID
     */
    Optional<CustomerResponse> getCustomerById(Long id);

    /**
     * Get customer by username
     */
    Optional<CustomerResponse> getCustomerByUsername(String username);

    /**
     * Get customer by email
     */
    Optional<CustomerResponse> getCustomerByEmail(String email);

    /**
     * Get all customers with pagination
     */
    Page<CustomerResponse> getAllCustomers(Pageable pageable);

    CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request);

    void deleteCustomer(Long id);

    /**
     * Activate customer
     */
    CustomerResponse activateCustomer(Long id);

    /**
     * Deactivate customer
     */
    CustomerResponse deactivateCustomer(Long id);

    /**
     * Verify customer email
     */
    CustomerResponse verifyCustomerEmail(Long id);

    /**
     * Search customers by criteria
     */
    Page<CustomerResponse> searchCustomers(String name, String email, Customer.CustomerRole role, 
                                         Boolean isActive, Pageable pageable);

    /**
     * Get customers by role
     */
    List<CustomerResponse> getCustomersByRole(Customer.CustomerRole role);

    /**
     * Get active customers
     */
    List<CustomerResponse> getActiveCustomers();

    /**
     * Get customers by city
     */
    List<CustomerResponse> getCustomersByCity(String city);

    /**
     * Get customers by country
     */
    List<CustomerResponse> getCustomersByCountry(String country);

    /**
     * Get top customers by order count
     */
    Page<CustomerResponse> getTopCustomersByOrderCount(Pageable pageable);

    /**
     * Get customers with no orders
     */
    List<CustomerResponse> getCustomersWithNoOrders();

    boolean usernameExists(String username);

    boolean emailExists(String email);

    /**
     * Get customer count by role
     */
    long getCustomerCountByRole(Customer.CustomerRole role);

    /**
     * Get total active customer count
     */
    long getActiveCustomerCount();

    /**
     * Get customer entity by username (for authentication)
     */
    Optional<Customer> getCustomerEntityByUsername(String username);

    /**
     * Get customer entity by email (for authentication)
     */
    Optional<Customer> getCustomerEntityByEmail(String email);
}
