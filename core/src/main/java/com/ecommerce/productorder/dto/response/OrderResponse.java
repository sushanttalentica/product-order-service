package com.ecommerce.productorder.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    String orderNumber,
    Long customerId,
    String customerEmail,
    String status,
    BigDecimal totalAmount,
    String shippingAddress,
    List<OrderItemResponse> orderItems,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public record OrderItemResponse(
      Long id, ProductInfo product, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    public record ProductInfo(Long id, String name, String sku) {}
  }
}
