package com.ecommerce.productorder.payment.dto.response;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
    Long id,
    String paymentId,
    Long orderId,
    Long customerId,
    BigDecimal amount,
    String status,
    String paymentMethod,
    String transactionId,
    String gatewayResponse,
    String failureReason,
    LocalDateTime processedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static PaymentResponse fromEntity(Payment payment) {
    return new PaymentResponse(
        payment.getId(),
        payment.getPaymentId(),
        payment.getOrderId(),
        payment.getCustomerId(),
        payment.getAmount(),
        payment.getStatus().name(),
        payment.getPaymentMethod().name(),
        payment.getTransactionId(),
        payment.getGatewayResponse(),
        payment.getFailureReason(),
        payment.getProcessedAt(),
        payment.getCreatedAt(),
        payment.getUpdatedAt());
  }

  public static PaymentResponse createMinimal(String paymentId, String status) {
    return new PaymentResponse(
        null, paymentId, null, null, null, status, null, null, null, null, null, null, null);
  }
}
