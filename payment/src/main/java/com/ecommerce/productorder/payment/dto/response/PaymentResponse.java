package com.ecommerce.productorder.payment.dto.response;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import com.ecommerce.productorder.payment.domain.entity.Payment.PaymentMethod;
import com.ecommerce.productorder.payment.domain.entity.Payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
    Long id,
    String paymentId,
    Long orderId,
    Long customerId,
    BigDecimal amount,
    PaymentStatus status,
    PaymentMethod paymentMethod,
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
        payment.getStatus(),
        payment.getPaymentMethod(),
        payment.getTransactionId(),
        payment.getGatewayResponse(),
        payment.getFailureReason(),
        payment.getProcessedAt(),
        payment.getCreatedAt(),
        payment.getUpdatedAt());
  }

  public static PaymentResponse createMinimal(String paymentId, PaymentStatus status) {
    return new PaymentResponse(
        null, paymentId, null, null, null, status, null, null, null, null, null, null, null);
  }
}
