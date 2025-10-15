package com.ecommerce.productorder.dto.request;

import com.ecommerce.productorder.domain.entity.Customer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCustomerRequest {

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
  private String username;

  @NotBlank(message = "Password is required")
  @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
  private String password;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "First name is required")
  @Size(max = 50, message = "First name must not exceed 50 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 50, message = "Last name must not exceed 50 characters")
  private String lastName;

  @Size(max = 20, message = "Phone number must not exceed 20 characters")
  private String phoneNumber;

  @Valid private AddressDto address;

  @NotNull(message = "Role is required")
  private Customer.CustomerRole role;

  @Getter
  @Setter
  public static class AddressDto {

    @Size(max = 200, message = "Street address must not exceed 200 characters")
    private String streetAddress;

    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    public AddressDto() {}

    public AddressDto(
        String streetAddress, String city, String state, String postalCode, String country) {
      this.streetAddress = streetAddress;
      this.city = city;
      this.state = state;
      this.postalCode = postalCode;
      this.country = country;
    }
  }

  public CreateCustomerRequest() {}

  public CreateCustomerRequest(
      String username,
      String password,
      String email,
      String firstName,
      String lastName,
      String phoneNumber,
      AddressDto address,
      Customer.CustomerRole role) {
    this.username = username;
    this.password = password;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.phoneNumber = phoneNumber;
    this.address = address;
    this.role = role;
  }
}
