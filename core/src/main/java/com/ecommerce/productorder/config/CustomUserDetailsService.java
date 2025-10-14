package com.ecommerce.productorder.config;

import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.domain.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Custom User Details Service
 * 
 * This service provides user details for authentication.
 * It loads users from the Customer entity in the database.
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerService customerService;    
    public CustomUserDetailsService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for username: {}", username);
        
        // Load from database
        return customerService.getCustomerEntityByUsername(username)
                .map(this::mapCustomerToUserDetails)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }

    private UserDetails mapCustomerToUserDetails(Customer customer) {
        log.info("Found customer in database: {}", customer.getUsername());
        
        return new User(
                customer.getUsername(),
                customer.getPassword(),
                customer.getIsActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                !customer.getIsActive().equals(false), // accountNonLocked
                getAuthority(customer.getRole().name())
        );
    }

    private List<SimpleGrantedAuthority> getAuthority(String role) {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
