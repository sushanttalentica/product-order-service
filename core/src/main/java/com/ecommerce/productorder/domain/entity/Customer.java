package com.ecommerce.productorder.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "customers")
@Getter
@Setter
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

  @Embedded private Address address;

  @Enumerated(EnumType.STRING)
  @NotNull(message = "Role is required")
  @Column(nullable = false)
  private CustomerRole role = CustomerRole.CUSTOMER;

  @Column(name = "is_active")
  private Boolean active = true;

  @Column(name = "email_verified")
  private Boolean emailVerified = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Order> orders;

  public Customer() {}

  public Customer(
      Long id,
      String username,
      String password,
      String email,
      String firstName,
      String lastName,
      String phoneNumber,
      Address address,
      CustomerRole role,
      Boolean active,
      Boolean emailVerified,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      List<Order> orders) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
    this.address = address;
    this.role = role;
    this.active = active;
    this.emailVerified = emailVerified;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.orders = orders;
  }

  // Customer Roles
  public enum CustomerRole {
    CUSTOMER,
    ADMIN,
    GUEST
  }

  // Address Embeddable
  @Embeddable
  @Getter
  @Setter
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

    public Address() {}

    public Address(
        String streetAddress, String city, String state, String postalCode, String country) {
      this.streetAddress = streetAddress;
      this.city = city;
      this.state = state;
      this.postalCode = postalCode;
      this.country = country;
    }
  }

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
    return active && hasRole(CustomerRole.CUSTOMER);
  }
}
