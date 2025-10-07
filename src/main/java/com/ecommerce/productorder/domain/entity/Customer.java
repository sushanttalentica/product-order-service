package com.ecommerce.productorder.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer Entity
 * 
 * Represents a customer in the e-commerce system.
 * 
 * Design Principles Applied:
 * - Domain-Driven Design: Represents the customer domain concept
 * - JPA Entity: Mapped to database table with proper annotations
 * - Validation: Uses Bean Validation for data integrity
 * - Audit Fields: Includes created/updated timestamps
 * - Builder Pattern: Uses Lombok builder for object creation
 */
@Entity
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role is required")
    @Column(nullable = false)
    @Builder.Default
    private CustomerRole role = CustomerRole.CUSTOMER;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;

    /**
     * Customer Role Enum
     */
    public enum CustomerRole {
        CUSTOMER, ADMIN, GUEST
    }

    /**
     * Address Embeddable
     */
    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @Column(name = "street_address")
        private String streetAddress;

        @Column(name = "city")
        private String city;

        @Column(name = "state")
        private String state;

        @Column(name = "postal_code")
        private String postalCode;

        @Column(name = "country")
        private String country;
    }

    // Helper methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasRole(CustomerRole role) {
        return this.role == role;
    }

    public boolean isAdmin() {
        return hasRole(CustomerRole.ADMIN);
    }

    public boolean isActiveCustomer() {
        return isActive && hasRole(CustomerRole.CUSTOMER);
    }
}
