package com.ecommerce.productorder.domain.service;

import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.dto.request.CreateCustomerRequest;
import com.ecommerce.productorder.dto.request.UpdateCustomerRequest;
import com.ecommerce.productorder.dto.response.CustomerResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

  CustomerResponse createCustomer(CreateCustomerRequest request);

  Optional<CustomerResponse> getCustomerById(Long id);

  Optional<CustomerResponse> getCustomerByUsername(String username);

  Optional<CustomerResponse> getCustomerByEmail(String email);

  Page<CustomerResponse> getAllCustomers(Pageable pageable);

  CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request);

  void deleteCustomer(Long id);

  CustomerResponse activateCustomer(Long id);

  CustomerResponse deactivateCustomer(Long id);

  CustomerResponse verifyCustomerEmail(Long id);

  Page<CustomerResponse> searchCustomers(
      String name, String email, Customer.CustomerRole role, Boolean isActive, Pageable pageable);

  List<CustomerResponse> getCustomersByRole(Customer.CustomerRole role);

  List<CustomerResponse> getActiveCustomers();

  List<CustomerResponse> getCustomersByCity(String city);

  List<CustomerResponse> getCustomersByCountry(String country);

  Page<CustomerResponse> getTopCustomersByOrderCount(Pageable pageable);

  List<CustomerResponse> getCustomersWithNoOrders();

  boolean usernameExists(String username);

  boolean emailExists(String email);

  long getCustomerCountByRole(Customer.CustomerRole role);

  long getActiveCustomerCount();

  Optional<Customer> getCustomerEntityByUsername(String username);

  Optional<Customer> getCustomerEntityByEmail(String email);
}
