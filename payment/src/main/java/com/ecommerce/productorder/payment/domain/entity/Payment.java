package com.ecommerce.productorder.payment.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "payment_id", unique = true, nullable = false, length = 50)
    private String paymentId;
    
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "refunded_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    
    @Column(name = "gateway_response", length = 1000)
    private String gatewayResponse;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Payment() {
    }
    
    public Payment(Long id, String paymentId, Long orderId, Long customerId,
                  BigDecimal amount, BigDecimal refundedAmount, PaymentStatus status,
                  PaymentMethod paymentMethod, String transactionId, String gatewayResponse,
                  String failureReason, LocalDateTime processedAt,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.refundedAmount = refundedAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.gatewayResponse = gatewayResponse;
        this.failureReason = failureReason;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public boolean canBeProcessed() {
        return status == PaymentStatus.PENDING;
    }
    
    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED || status == PaymentStatus.PARTIALLY_REFUNDED;
    }
    
    public void processPayment(String transactionId, String gatewayResponse) {
        if (!canBeProcessed()) {
            throw new IllegalStateException("Payment cannot be processed in current state: " + status);
        }
        
        this.transactionId = transactionId;
        this.gatewayResponse = gatewayResponse;
        this.refundedAmount = BigDecimal.ZERO;
        this.status = PaymentStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }
    
    public void failPayment(String failureReason) {
        if (!canBeProcessed()) {
            throw new IllegalStateException("Payment cannot be failed in current state: " + status);
        }
        
        this.failureReason = failureReason;
        this.status = PaymentStatus.FAILED;
        this.processedAt = LocalDateTime.now();
    }
    
    public void refundPayment(BigDecimal refundAmount) {
        if (!canBeRefunded()) {
            throw new IllegalStateException("Payment cannot be refunded in current state: " + status);
        }
        
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero");
        }
        
        if (this.refundedAmount == null) {
            this.refundedAmount = BigDecimal.ZERO;
        }
        
        BigDecimal remainingRefundable = amount.subtract(refundedAmount);
        if (remainingRefundable.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Payment already fully refunded");
        }
        
        if (refundAmount.compareTo(remainingRefundable) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed remaining refundable amount: " + remainingRefundable);
        }
        
        this.refundedAmount = this.refundedAmount.add(refundAmount);
        
        if (this.refundedAmount.compareTo(amount) == 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    // Calculate remaining refundable amount
    public BigDecimal getRemainingRefundableAmount() {
        return amount.subtract(refundedAmount == null ? BigDecimal.ZERO : refundedAmount);
    }
    
    // Enum representing payment status states
    public enum PaymentStatus {
        PENDING,           // Payment initiated, waiting for processing
        PROCESSING,        // Payment being processed by gateway
        COMPLETED,         // Payment successfully completed
        FAILED,           // Payment failed
        CANCELLED,        // Payment cancelled
        REFUNDED,         // Payment fully refunded
        PARTIALLY_REFUNDED // Payment partially refunded
    }
    
    // Enum representing payment methods
    public enum PaymentMethod {
        CREDIT_CARD,
        DEBIT_CARD,
        PAYPAL,
        STRIPE,
        PAYPAL_WALLET,
        BANK_TRANSFER
    }
    
}
