package com.ecommerce.productorder.domain.repository;

import com.ecommerce.productorder.domain.entity.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

  Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

  Page<Product> findByActiveTrue(Pageable pageable);

  Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);

  Page<Product> findByPriceBetweenAndActiveTrue(
      java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Pageable pageable);

  @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.active = true")
  List<Product> findProductsWithLowStock(@Param("threshold") Integer threshold);

  Optional<Product> findBySkuAndActiveTrue(String sku);

  @Query(
      """
        SELECT p FROM Product p
        WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND p.active = true
        """)
  Page<Product> findProductsByCriteria(
      @Param("name") String name,
      @Param("categoryId") Long categoryId,
      @Param("minPrice") java.math.BigDecimal minPrice,
      @Param("maxPrice") java.math.BigDecimal maxPrice,
      Pageable pageable);

  boolean existsBySkuAndActiveTrue(String sku);

  Page<Product> findByCategoryNameContainingIgnoreCaseAndActiveTrue(
      String categoryName, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Product p WHERE p.id = :productId")
  Optional<Product> findByIdWithLock(@Param("productId") Long productId);

  @Modifying
  @Query(
      """
        UPDATE Product p
        SET p.stockQuantity = p.stockQuantity - :quantity
        WHERE p.id = :productId
        AND p.stockQuantity >= :quantity
        """)
  int reduceStockAtomic(@Param("productId") Long productId, @Param("quantity") Integer quantity);

  @Modifying
  @Query(
      """
        UPDATE Product p
        SET p.stockQuantity = p.stockQuantity + :quantity
        WHERE p.id = :productId
        """)
  int restoreStockAtomic(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
