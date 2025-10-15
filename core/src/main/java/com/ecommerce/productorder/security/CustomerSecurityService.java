package com.ecommerce.productorder.security;

import com.ecommerce.productorder.domain.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerSecurityService {

  private final CustomerService customerService;

  public CustomerSecurityService(CustomerService customerService) {
    this.customerService = customerService;
  }

  public boolean isOwner(Long customerId, String username) {
    log.debug("Checking ownership for customer ID: {} and username: {}", customerId, username);

    return customerService
        .getCustomerByUsername(username)
        .map(customer -> customer.id().equals(customerId))
        .orElse(false);
  }

  // Get customer ID from authentication
  public Long getCustomerIdFromAuth(Authentication authentication) {
    if (authentication == null || authentication.getName() == null) {
      return null;
    }

    return customerService
        .getCustomerByUsername(authentication.getName())
        .map(customer -> customer.id())
        .orElse(null);
  }
}
