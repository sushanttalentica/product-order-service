package com.ecommerce.productorder.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version private Long version; // Optimistic locking for concurrent stock updates

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
  private Boolean active = true;

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

  public Product() {}

  public Product(
      Long id,
      Long version,
      String name,
      String description,
      BigDecimal price,
      Integer stockQuantity,
      String sku,
      Boolean active,
      Category category,
      List<OrderItem> orderItems,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.version = version;
    this.name = name;
    this.description = description;
    this.price = price;
    this.stockQuantity = stockQuantity;
    this.sku = sku;
    this.active = active;
    this.category = category;
    this.orderItems = orderItems;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public boolean isAvailableForPurchase() {
    return active != null && active && stockQuantity > 0;
  }

  // Business method to check if sufficient stock is available
  public boolean hasSufficientStock(int requestedQuantity) {
    return stockQuantity >= requestedQuantity;
  }

  public void reduceStock(int quantity) {
    if (!hasSufficientStock(quantity)) {
      throw new IllegalArgumentException("Insufficient stock available");
    }
    this.stockQuantity -= quantity;
  }

  public void restoreStock(int quantity) {
    this.stockQuantity += quantity;
  }
}
