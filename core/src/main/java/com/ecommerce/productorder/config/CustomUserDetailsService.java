package com.ecommerce.productorder.config;

import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.domain.service.CustomerService;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Custom UserDetailsService to load user details from the database
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
    return customerService
        .getCustomerEntityByUsername(username)
        .map(this::mapCustomerToUserDetails)
        .orElseThrow(
            () -> {
              log.warn("User not found: {}", username);
              return new UsernameNotFoundException("User not found: " + username);
            });
  }

  private UserDetails mapCustomerToUserDetails(Customer customer) {
    log.info("Found customer in database: {}", customer.getUsername());
    List<SimpleGrantedAuthority> authorities = getAuthority(customer.getRole().name());
    log.info("User {} has authorities: {}", customer.getUsername(), authorities);

    return new User(
        customer.getUsername(),
        customer.getPassword(),
        customer.getActive(),
        true, // accountNonExpired
        true, // credentialsNonExpired
        !customer.getActive().equals(false), // accountNonLocked
        authorities);
  }

  private List<SimpleGrantedAuthority> getAuthority(String role) {
    return Arrays.asList(new SimpleGrantedAuthority("ROLE_" + role));
  }
}
