package com.ecommerce.productorder.service;

import com.ecommerce.productorder.domain.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Customer Security Service
 * 
 * Service for customer security and authorization checks.
 * 
 * Design Principles Applied:
 * - Security Service: Handles authorization logic
 * - Single Responsibility: Focuses on security-related operations
 * - Dependency Injection: Uses CustomerService for data access
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerSecurityService {

    private final CustomerService customerService;

    /**
     * Check if the authenticated user owns the customer resource
     */
    public boolean isOwner(Long customerId, String username) {
        log.debug("Checking ownership for customer ID: {} and username: {}", customerId, username);
        
        return customerService.getCustomerByUsername(username)
                .map(customer -> customer.getId().equals(customerId))
                .orElse(false);
    }

    /**
     * Get customer ID from authentication
     */
    public Long getCustomerIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        return customerService.getCustomerByUsername(authentication.getName())
                .map(customer -> customer.getId())
                .orElse(null);
    }
}
