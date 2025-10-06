package com.ecommerce.productorder.invoice.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Invoice entity representing generated invoices
 * 
 * Design Principles Applied:
 * - Domain-Driven Design: Core business entity for invoice domain
 * - Single Responsibility: Manages only invoice-related data and behavior
 * - Encapsulation: All invoice data and business rules encapsulated
 * - Value Objects: Uses BigDecimal for monetary precision
 * - State Pattern: Invoice status represents different states
 * - Aggregate Root: Invoice is the aggregate root for invoice operations
 * - Immutability: Uses Builder pattern for object creation
 * - JPA Best Practices: Proper entity mapping with constraints
 */
@Entity
@Table(name = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.GENERATED;
    
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Business method to check if invoice can be downloaded
     * Encapsulates invoice access business rules
     * 
     * @return true if invoice can be downloaded, false otherwise
     */
    public boolean canBeDownloaded() {
        return status == InvoiceStatus.GENERATED || status == InvoiceStatus.SENT;
    }
    
    /**
     * Business method to check if invoice can be regenerated
     * Encapsulates invoice regeneration business rules
     * 
     * @return true if invoice can be regenerated, false otherwise
     */
    public boolean canBeRegenerated() {
        return status == InvoiceStatus.GENERATED || status == InvoiceStatus.SENT;
    }
    
    /**
     * Business method to mark invoice as sent
     * Encapsulates invoice status update logic
     */
    public void markAsSent() {
        if (status != InvoiceStatus.GENERATED) {
            throw new IllegalStateException("Invoice cannot be marked as sent in current state: " + status);
        }
        this.status = InvoiceStatus.SENT;
    }
    
    /**
     * Business method to mark invoice as failed
     * Encapsulates invoice failure logic
     * 
     * @param failureReason the reason for failure
     */
    public void markAsFailed(String failureReason) {
        this.status = InvoiceStatus.FAILED;
    }
    
    /**
     * Enum representing invoice status states
     * Uses State Pattern for invoice lifecycle management
     */
    public enum InvoiceStatus {
        GENERATED,  // Invoice generated successfully
        SENT,       // Invoice sent to customer
        FAILED,     // Invoice generation failed
        DELETED     // Invoice deleted
    }
}
