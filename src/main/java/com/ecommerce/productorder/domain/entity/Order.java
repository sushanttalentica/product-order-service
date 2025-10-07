package com.ecommerce.productorder.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order entity representing customer orders
 * 
 * Design Principles Applied:
 * - Domain-Driven Design: Core business aggregate root
 * - Single Responsibility: Manages order data and business rules
 * - Encapsulation: All order-related data and behavior encapsulated
 * - Aggregate Pattern: Order is the aggregate root for OrderItems
 * - State Pattern: Order status represents different states
 */
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_number", unique = true, nullable = false, length = 50)
    private String orderNumber;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", insertable = false, updatable = false)
    private Customer customer;
    
    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Business method to calculate total amount from order items
     * Encapsulates order calculation logic
     */
    public BigDecimal calculateTotalAmount() {
        return orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Business method to check if order can be cancelled
     * Encapsulates order state business rules
     */
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    /**
     * Business method to update order status
     * Encapsulates state transition logic
     */
    public void updateStatus(OrderStatus newStatus) {
        if (isValidStatusTransition(this.status, newStatus)) {
            this.status = newStatus;
        } else {
            throw new IllegalArgumentException("Invalid status transition from " + this.status + " to " + newStatus);
        }
    }
    
    /**
     * Business method to validate status transitions
     * Encapsulates order state machine logic
     */
    private boolean isValidStatusTransition(OrderStatus current, OrderStatus newStatus) {
        return switch (current) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED -> newStatus == OrderStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
    
    /**
     * Enum representing order status states
     * Uses State Pattern for order lifecycle management
     */
    public enum OrderStatus {
        PENDING,    // Order created, waiting for payment
        CONFIRMED,  // Payment confirmed
        PROCESSING, // Order being prepared
        SHIPPED,    // Order shipped
        DELIVERED,  // Order delivered
        COMPLETED,  // Order completed
        CANCELLED   // Order cancelled
    }
}
