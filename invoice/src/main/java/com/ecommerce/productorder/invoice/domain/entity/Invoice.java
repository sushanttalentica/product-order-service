package com.ecommerce.productorder.invoice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@Builder
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;
    
    @Column(name = "s3_url", nullable = false, length = 1000)
    private String s3Url;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status = InvoiceStatus.GENERATED;
    
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Invoice() {
    }
    
    public Invoice(Long id, Long orderId, Long customerId, String customerEmail,
                  BigDecimal totalAmount, String s3Key, String s3Url,
                  InvoiceStatus status, LocalDateTime generatedAt,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.s3Key = s3Key;
        this.s3Url = s3Url;
        this.status = status;
        this.generatedAt = generatedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public boolean canBeDownloaded() {
        return status == InvoiceStatus.GENERATED || status == InvoiceStatus.SENT;
    }
    
    public boolean canBeRegenerated() {
        return status == InvoiceStatus.GENERATED || status == InvoiceStatus.SENT;
    }
    
    public void markAsSent() {
        if (status != InvoiceStatus.GENERATED) {
            throw new IllegalStateException("Invoice cannot be marked as sent in current state: " + status);
        }
        this.status = InvoiceStatus.SENT;
    }
    
    public void markAsFailed(String failureReason) {
        this.status = InvoiceStatus.FAILED;
    }

     // Enum representing invoice status states

    public enum InvoiceStatus {
        GENERATED,
        SENT,
        FAILED,
        DELETED
    }
    
}
