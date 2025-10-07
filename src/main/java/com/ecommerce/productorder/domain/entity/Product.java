package com.ecommerce.productorder.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * Product entity representing e-commerce products
 * 
 * Design Principles Applied:
 * - Domain-Driven Design: Core business entity
 * - Single Responsibility: Manages product data and business rules
 * - Encapsulation: All product-related data and behavior encapsulated
 * - Value Objects: Uses BigDecimal for monetary values to avoid precision issues
 * - JPA Best Practices: Proper entity relationships and constraints
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    @Column(name = "sku", unique = true, length = 50)
    private String sku;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderItem> orderItems;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Business method to check if product is available for purchase
     * Encapsulates business logic within the domain entity
     */
    public boolean isAvailableForPurchase() {
        return isActive != null && isActive && stockQuantity > 0;
    }
    
    /**
     * Business method to check if sufficient stock is available
     * Encapsulates inventory business rules
     */
    public boolean hasSufficientStock(int requestedQuantity) {
        return stockQuantity >= requestedQuantity;
    }
    
    /**
     * Business method to reduce stock after order
     * Encapsulates inventory management logic
     */
    public void reduceStock(int quantity) {
        if (!hasSufficientStock(quantity)) {
            throw new IllegalArgumentException("Insufficient stock available");
        }
        this.stockQuantity -= quantity;
    }
    
    /**
     * Business method to restore stock (for order cancellation)
     * Encapsulates inventory restoration logic
     */
    public void restoreStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
