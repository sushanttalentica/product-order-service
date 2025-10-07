package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.domain.entity.Customer;
import com.ecommerce.productorder.domain.service.CustomerService;
import com.ecommerce.productorder.dto.request.CreateCustomerRequest;
import com.ecommerce.productorder.dto.request.UpdateCustomerRequest;
import com.ecommerce.productorder.dto.response.CustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Customer Controller
 * 
 * REST Controller for Customer operations.
 * 
 * Design Principles Applied:
 * - RESTful Design: Follows REST conventions for HTTP methods and status codes
 * - Security: Uses @PreAuthorize for role-based access control
 * - Validation: Uses Bean Validation for input validation
 * - Error Handling: Delegates to global exception handler
 * - Logging: Uses SLF4J for logging
 * - Pagination: Supports pagination for list operations
 * - Swagger Documentation: Provides comprehensive API documentation
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customers", description = "Customer management operations")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Create a new customer
     */
    @PostMapping
    @Operation(summary = "Create a new customer", description = "Create a new customer account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Customer created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid customer data"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    public ResponseEntity<CustomerResponse> createCustomer(
            @Parameter(description = "Customer creation data") @Valid @RequestBody CreateCustomerRequest request) {
        log.info("Creating customer with username: {}", request.getUsername());
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get customer by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieve customer information by ID")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @customerSecurityService.isOwner(#id, authentication.name))")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer found"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CustomerResponse> getCustomerById(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        log.debug("Retrieving customer with ID: {}", id);
        Optional<CustomerResponse> customer = customerService.getCustomerById(id);
        return customer.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get customer by username
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Get customer by username", description = "Retrieve customer information by username")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer found"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<CustomerResponse> getCustomerByUsername(
            @Parameter(description = "Customer username") @PathVariable String username) {
        log.debug("Retrieving customer with username: {}", username);
        Optional<CustomerResponse> customer = customerService.getCustomerByUsername(username);
        return customer.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all customers with pagination
     */
    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieve a paginated list of all customers")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved customers"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving all customers with pagination: {}", pageable);
        Page<CustomerResponse> response = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Update customer
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Update customer information")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CUSTOMER') and @customerSecurityService.isOwner(#id, authentication.name))")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid customer data"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<CustomerResponse> updateCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long id,
            @Parameter(description = "Customer update data") @Valid @RequestBody UpdateCustomerRequest request) {
        log.info("Updating customer with ID: {}", id);
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete customer
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Delete customer account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        log.info("Deleting customer with ID: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Activate customer
     */
    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate customer", description = "Activate customer account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer activated successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<CustomerResponse> activateCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        log.info("Activating customer with ID: {}", id);
        CustomerResponse response = customerService.activateCustomer(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate customer
     */
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate customer", description = "Deactivate customer account (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<CustomerResponse> deactivateCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        log.info("Deactivating customer with ID: {}", id);
        CustomerResponse response = customerService.deactivateCustomer(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify customer email
     */
    @PatchMapping("/{id}/verify-email")
    @Operation(summary = "Verify customer email", description = "Mark customer email as verified")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer email verified successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<CustomerResponse> verifyCustomerEmail(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        log.info("Verifying email for customer with ID: {}", id);
        CustomerResponse response = customerService.verifyCustomerEmail(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Search customers
     */
    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Search customers by various criteria")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Page<CustomerResponse>> searchCustomers(
            @Parameter(description = "Name to search") @RequestParam(required = false) String name,
            @Parameter(description = "Email to search") @RequestParam(required = false) String email,
            @Parameter(description = "Customer role") @RequestParam(required = false) Customer.CustomerRole role,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Searching customers with criteria - name: {}, email: {}, role: {}, isActive: {}", 
                 name, email, role, isActive);
        Page<CustomerResponse> response = customerService.searchCustomers(name, email, role, isActive, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get customers by role
     */
    @GetMapping("/role/{role}")
    @Operation(summary = "Get customers by role", description = "Retrieve customers by their role")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<List<CustomerResponse>> getCustomersByRole(
            @Parameter(description = "Customer role") @PathVariable Customer.CustomerRole role) {
        log.debug("Retrieving customers by role: {}", role);
        List<CustomerResponse> response = customerService.getCustomersByRole(role);
        return ResponseEntity.ok(response);
    }

    /**
     * Get active customers
     */
    @GetMapping("/active")
    @Operation(summary = "Get active customers", description = "Retrieve all active customers")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active customers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<List<CustomerResponse>> getActiveCustomers() {
        log.debug("Retrieving active customers");
        List<CustomerResponse> response = customerService.getActiveCustomers();
        return ResponseEntity.ok(response);
    }

    /**
     * Get customers by city
     */
    @GetMapping("/city/{city}")
    @Operation(summary = "Get customers by city", description = "Retrieve customers by city")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<List<CustomerResponse>> getCustomersByCity(
            @Parameter(description = "City name") @PathVariable String city) {
        log.debug("Retrieving customers by city: {}", city);
        List<CustomerResponse> response = customerService.getCustomersByCity(city);
        return ResponseEntity.ok(response);
    }

    /**
     * Get top customers by order count
     */
    @GetMapping("/top")
    @Operation(summary = "Get top customers", description = "Retrieve top customers by order count")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Top customers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Page<CustomerResponse>> getTopCustomersByOrderCount(
            @Parameter(description = "Pagination parameters") @PageableDefault(size = 10) Pageable pageable) {
        log.debug("Retrieving top customers by order count");
        Page<CustomerResponse> response = customerService.getTopCustomersByOrderCount(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get customer statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get customer statistics", description = "Retrieve customer statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<CustomerStatistics> getCustomerStatistics() {
        log.debug("Retrieving customer statistics");
        
        long totalCustomers = customerService.getActiveCustomerCount();
        long adminCount = customerService.getCustomerCountByRole(Customer.CustomerRole.ADMIN);
        long customerCount = customerService.getCustomerCountByRole(Customer.CustomerRole.CUSTOMER);
        
        CustomerStatistics stats = CustomerStatistics.builder()
                .totalCustomers(totalCustomers)
                .adminCount(adminCount)
                .customerCount(customerCount)
                .build();
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Customer Statistics DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CustomerStatistics {
        private long totalCustomers;
        private long adminCount;
        private long customerCount;
    }
}
