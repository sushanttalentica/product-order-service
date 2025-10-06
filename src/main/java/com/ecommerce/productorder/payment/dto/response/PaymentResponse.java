package com.ecommerce.productorder.payment.dto.response;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment operations
 * 
 * Design Principles Applied:
 * - Data Transfer Object: Encapsulates payment response data
 * - Immutability: Uses Builder pattern for object creation
 * - Single Responsibility: Only handles payment response data
 * - Encapsulation: All payment response data encapsulated
 * - Value Objects: Uses BigDecimal for monetary precision
 * - Factory Pattern: Uses static factory methods for creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long id;
    private String paymentId;
    private Long orderId;
    private Long customerId;
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private String gatewayResponse;
    private String failureReason;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Factory method to create PaymentResponse from Payment entity
     * Uses Factory Pattern for object creation
     * 
     * @param payment the Payment entity to convert
     * @return PaymentResponse created from Payment entity
     */
    public static PaymentResponse fromEntity(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .gatewayResponse(payment.getGatewayResponse())
                .failureReason(payment.getFailureReason())
                .processedAt(payment.getProcessedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
    
    /**
     * Factory method to create PaymentResponse with minimal data
     * Uses Factory Pattern for object creation
     * 
     * @param paymentId the payment ID
     * @param status the payment status
     * @return PaymentResponse with minimal data
     */
    public static PaymentResponse createMinimal(String paymentId, String status) {
        return PaymentResponse.builder()
                .paymentId(paymentId)
                .status(status)
                .build();
    }
}
