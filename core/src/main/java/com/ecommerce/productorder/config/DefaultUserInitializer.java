package com.ecommerce.productorder.config;

import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.domain.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Default User Initializer
 * 
 * This component creates default users (admin and customer) at application startup.
 * It ensures that basic users are always available for testing and demo purposes.
 */
@Component
@Slf4j
public class DefaultUserInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;    
    public DefaultUserInitializer(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Initializing default users...");
        
        // Create default admin user
        createDefaultAdmin();
        
        // Create default customer user
        createDefaultCustomer();
        
        log.info("Default users initialization completed");
    }

    private void createDefaultAdmin() {
        String adminUsername = "admin";
        
        if (customerRepository.existsByUsername(adminUsername)) {
            log.info("Admin user already exists, skipping creation");
            return;
        }

        Customer admin = Customer.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode("admin123"))
                .email("admin@gmail.com")
                .firstName("Admin")
                .lastName("User")
                .role(Customer.CustomerRole.ADMIN)
                .isActive(true)
                .emailVerified(true)
                .build();

        customerRepository.save(admin);
        log.info("Default admin user created: {} / admin123", adminUsername);
    }

    private void createDefaultCustomer() {
        String customerUsername = "customer";
        
        if (customerRepository.existsByUsername(customerUsername)) {
            log.info("Customer user already exists, skipping creation");
            return;
        }

        Customer customer = Customer.builder()
                .username(customerUsername)
                .password(passwordEncoder.encode("customer123"))
                .email("customer@gmail.com")
                .firstName("Customer")
                .lastName("User")
                .role(Customer.CustomerRole.CUSTOMER)
                .isActive(true)
                .emailVerified(true)
                .build();

        customerRepository.save(customer);
        log.info("Default customer user created: {} / customer123", customerUsername);
    }
}
