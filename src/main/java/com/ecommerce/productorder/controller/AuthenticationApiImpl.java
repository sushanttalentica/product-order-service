package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.api.AuthenticationApi;
import com.ecommerce.productorder.api.model.AuthResponse;
import com.ecommerce.productorder.api.model.LoginRequest;
import com.ecommerce.productorder.api.model.MessageResponse;
import com.ecommerce.productorder.api.model.RegisterRequest;
import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.domain.service.CustomerService;
import com.ecommerce.productorder.dto.request.CreateCustomerRequest;
import com.ecommerce.productorder.dto.response.CustomerResponse;
import com.ecommerce.productorder.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthenticationApiImpl implements AuthenticationApi {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final CustomerService customerService;

    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            
            log.info("Login successful for user: {}", loginRequest.getUsername());
            
            return ResponseEntity.ok(new AuthResponse()
                    .token(token)
                    .username(loginRequest.getUsername())
                    .message("Login successful")
                    .role(null));
                    
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(new AuthResponse()
                            .message("Invalid credentials"));
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {}", loginRequest.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(new AuthResponse()
                            .message("Login failed: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<AuthResponse> register(RegisterRequest registerRequest) {
        log.info("Registration request for user: {}", registerRequest.getUsername());
        try {
            CreateCustomerRequest.AddressDto addressDto = CreateCustomerRequest.AddressDto.builder()
                    .streetAddress(registerRequest.getStreetAddress())
                    .city(registerRequest.getCity())
                    .state(registerRequest.getState())
                    .postalCode(registerRequest.getPostalCode())
                    .country(registerRequest.getCountry())
                    .build();
            
            CreateCustomerRequest createRequest = CreateCustomerRequest.builder()
                    .username(registerRequest.getUsername())
                    .password(registerRequest.getPassword())
                    .email(registerRequest.getEmail())
                    .firstName(registerRequest.getFirstName())
                    .lastName(registerRequest.getLastName())
                    .phoneNumber(registerRequest.getPhoneNumber())
                    .address(addressDto)
                    .role(Customer.CustomerRole.CUSTOMER)
                    .build();

            CustomerResponse customerResponse = customerService.createCustomer(createRequest);
            log.info("Customer registered successfully: {}", registerRequest.getUsername());
            
            return ResponseEntity.status(201).body(new AuthResponse()
                    .username(customerResponse.username())
                    .message("Registration successful. Please login."));
                    
        } catch (Exception e) {
            log.error("Registration failed for user: {}", registerRequest.getUsername(), e);
            return ResponseEntity.badRequest()
                    .body(new AuthResponse()
                            .message("Registration failed: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<MessageResponse> healthCheck() {
        return ResponseEntity.ok(new MessageResponse()
                .message("Auth service is running")
                .success(true));
    }
}

