package com.ecommerce.productorder.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer stockQuantity,
    String sku,
    Boolean active,
    CategoryResponse category,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public record CategoryResponse(Long id, String name, String description, Boolean active) {}
}
