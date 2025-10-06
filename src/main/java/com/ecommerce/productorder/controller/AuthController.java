package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.domain.service.CustomerService;
import com.ecommerce.productorder.dto.request.CreateCustomerRequest;
import com.ecommerce.productorder.dto.request.LoginRequest;
import com.ecommerce.productorder.dto.request.RegisterRequest;
import com.ecommerce.productorder.dto.response.AuthResponse;
import com.ecommerce.productorder.dto.response.CustomerResponse;
import com.ecommerce.productorder.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Authentication Controller
 * 
 * This controller handles user authentication and registration.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and authorization")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final CustomerService customerService;

    /**
     * User Login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<AuthResponse> login(
            @Parameter(description = "Login credentials") @Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        log.info("Password provided: {}", request.getPassword());
        
        try {
            // Authenticate user
            log.info("Attempting authentication for user: {}", request.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            log.info("Authentication successful for user: {}", request.getUsername());
            
            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            log.info("User details loaded: {}", userDetails.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            log.info("JWT token generated successfully");
            
            log.info("Login successful for user: {}", request.getUsername());
            
            return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(request.getUsername())
                .message("Login successful")
                .build());
                
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("Authentication failed for user: {} - Error: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message("Invalid credentials")
                    .build());
        } catch (Exception e) {
            log.error("Unexpected error during login for user: {} - Error: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message("Login failed: " + e.getMessage())
                    .build());
        }
    }

    /**
     * User Registration
     */
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new customer account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Registration successful"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<AuthResponse> register(
            @Parameter(description = "Registration data") @Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());
        
        try {
            // Convert RegisterRequest to CreateCustomerRequest
            // Split fullName into firstName and lastName
            String[] nameParts = request.getFullName().trim().split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            CreateCustomerRequest createRequest = CreateCustomerRequest.builder()
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .email(request.getEmail())
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(Customer.CustomerRole.CUSTOMER) // Default to CUSTOMER role
                    .build();

            // Create customer
            CustomerResponse customerResponse = customerService.createCustomer(createRequest);
            log.info("Customer registered successfully: {}", request.getUsername());
            
            return ResponseEntity.status(201).body(AuthResponse.builder()
                .username(customerResponse.getUsername())
                .message("Registration successful. Please login.")
                .build());
                
        } catch (Exception e) {
            log.error("Registration failed for user: {} - Error: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(AuthResponse.builder()
                    .message("Registration failed: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Health Check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
