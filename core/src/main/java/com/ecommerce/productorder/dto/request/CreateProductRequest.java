package com.ecommerce.productorder.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductRequest {

  @NotBlank(message = "Product name is required")
  @Size(max = 200, message = "Product name must not exceed 200 characters")
  private String name;

  @Size(max = 1000, message = "Description must not exceed 1000 characters")
  private String description;

  @NotNull(message = "Price is required")
  @DecimalMin(value = "0.01", message = "Price must be greater than 0")
  @Digits(
      integer = 8,
      fraction = 2,
      message = "Price must have at most 8 integer digits and 2 decimal places")
  private BigDecimal price;

  @NotNull(message = "Stock quantity is required")
  @Min(value = 0, message = "Stock quantity must be non-negative")
  private Integer stockQuantity;

  @NotBlank(message = "SKU is required")
  @Size(max = 50, message = "SKU must not exceed 50 characters")
  @Pattern(
      regexp = "^[A-Z0-9-_]+$",
      message = "SKU must contain only uppercase letters, numbers, hyphens, and underscores")
  private String sku;

  @NotNull(message = "Category ID is required")
  @Positive(message = "Category ID must be positive")
  private Long categoryId;

  public CreateProductRequest() {}

  public CreateProductRequest(
      String name,
      String description,
      BigDecimal price,
      Integer stockQuantity,
      String sku,
      Long categoryId) {
    this.name = name;
    this.description = description;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.sku = sku;
    this.categoryId = categoryId;
  }
}
