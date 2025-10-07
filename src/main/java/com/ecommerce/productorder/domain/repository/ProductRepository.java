package com.ecommerce.productorder.domain.repository;

import com.ecommerce.productorder.domain.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 * 
 * Design Principles Applied:
 * - Repository Pattern: Abstracts data access logic
 * - Interface Segregation: Only exposes necessary data access methods
 * - Dependency Inversion: Depends on abstraction, not concrete implementation
 * - Spring Data JPA: Leverages Spring's repository abstraction
 * - Query Methods: Uses Spring Data's query derivation
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Find products by category with pagination
     * Uses Spring Data JPA query derivation
     */
    Page<Product> findByCategoryIdAndIsActiveTrue(Long categoryId, Pageable pageable);
    
    /**
     * Find active products with pagination
     * Uses Spring Data JPA query derivation
     */
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    /**
     * Find products by name containing (case-insensitive search)
     * Uses Spring Data JPA query derivation with custom method naming
     */
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);
    
    /**
     * Find products by price range
     * Uses Spring Data JPA query derivation
     */
    Page<Product> findByPriceBetweenAndIsActiveTrue(java.math.BigDecimal minPrice, 
                                                   java.math.BigDecimal maxPrice, 
                                                   Pageable pageable);
    
    /**
     * Find products with low stock (below threshold)
     * Uses custom JPQL query for complex business logic
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.isActive = true")
    List<Product> findProductsWithLowStock(@Param("threshold") Integer threshold);
    
    /**
     * Find product by SKU
     * Uses Spring Data JPA query derivation
     */
    Optional<Product> findBySkuAndIsActiveTrue(String sku);
    
    /**
     * Find products by multiple criteria with custom query
     * Uses JPQL for complex search scenarios
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "p.isActive = true")
    Page<Product> findProductsByCriteria(@Param("name") String name,
                                       @Param("categoryId") Long categoryId,
                                       @Param("minPrice") java.math.BigDecimal minPrice,
                                       @Param("maxPrice") java.math.BigDecimal maxPrice,
                                       Pageable pageable);
    
    /**
     * Check if product exists by SKU
     * Uses Spring Data JPA query derivation
     */
    boolean existsBySkuAndIsActiveTrue(String sku);
    
    /**
     * Find products by category name
     * Uses Spring Data JPA query derivation with join
     */
    Page<Product> findByCategoryNameContainingIgnoreCaseAndIsActiveTrue(String categoryName, Pageable pageable);
    
    /**
     * Find product by ID with pessimistic write lock
     * Prevents concurrent modifications to the same product
     * Used for stock updates to avoid race conditions
     * 
     * @param productId the product ID
     * @return Optional containing locked product if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdWithLock(@Param("productId") Long productId);
    
    /**
     * Atomically reduce product stock
     * Uses database-level atomic operation to prevent race conditions
     * Only updates if sufficient stock is available
     * 
     * @param productId the product ID
     * @param quantity the quantity to reduce
     * @return number of rows updated (1 if successful, 0 if insufficient stock)
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity " +
           "WHERE p.id = :productId AND p.stockQuantity >= :quantity")
    int reduceStockAtomic(@Param("productId") Long productId, 
                          @Param("quantity") Integer quantity);
    
    /**
     * Atomically restore product stock
     * Used for order cancellations and refunds
     * 
     * @param productId the product ID
     * @param quantity the quantity to restore
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantity " +
           "WHERE p.id = :productId")
    int restoreStockAtomic(@Param("productId") Long productId, 
                           @Param("quantity") Integer quantity);
}
