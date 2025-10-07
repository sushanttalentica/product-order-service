package com.ecommerce.productorder.domain.repository;

import com.ecommerce.productorder.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity
 * 
 * Design Principles Applied:
 * - Repository Pattern: Abstracts data access logic
 * - Interface Segregation: Only exposes necessary data access methods
 * - Dependency Inversion: Depends on abstraction, not concrete implementation
 * - Spring Data JPA: Leverages Spring's repository abstraction
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * Find orders by customer ID with pagination
     * Uses Spring Data JPA query derivation
     */
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    
    /**
     * Find orders by customer email with pagination
     * Uses Spring Data JPA query derivation
     */
    Page<Order> findByCustomerEmail(String customerEmail, Pageable pageable);
    
    /**
     * Find order by order number
     * Uses Spring Data JPA query derivation
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * Find orders by status with pagination
     * Uses Spring Data JPA query derivation
     */
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    /**
     * Find orders by customer and status
     * Uses Spring Data JPA query derivation
     */
    Page<Order> findByCustomerIdAndStatus(Long customerId, Order.OrderStatus status, Pageable pageable);
    
    /**
     * Find orders created between dates
     * Uses Spring Data JPA query derivation
     */
    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find orders by customer and date range
     * Uses Spring Data JPA query derivation
     */
    Page<Order> findByCustomerIdAndCreatedAtBetween(Long customerId, 
                                                   LocalDateTime startDate, 
                                                   LocalDateTime endDate, 
                                                   Pageable pageable);
    
    /**
     * Find orders with total amount greater than specified value
     * Uses Spring Data JPA query derivation
     */
    Page<Order> findByTotalAmountGreaterThan(java.math.BigDecimal amount, Pageable pageable);
    
    /**
     * Find orders with total amount between specified values
     * Uses Spring Data JPA query derivation
     */
    Page<Order> findByTotalAmountBetween(java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount, Pageable pageable);
    
    /**
     * Find orders by status and created before specified time
     * Uses Spring Data JPA query derivation
     */
    List<Order> findByStatusAndCreatedAtBefore(Order.OrderStatus status, LocalDateTime createdAt);
    
    /**
     * Find orders by multiple criteria
     * Uses custom JPQL query for complex search scenarios
     */
    @Query("SELECT o FROM Order o WHERE " +
           "(:customerId IS NULL OR o.customerId = :customerId) AND " +
           "(:status IS NULL OR o.status = :status) AND " +
           "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR o.createdAt <= :endDate) AND " +
           "(:minAmount IS NULL OR o.totalAmount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR o.totalAmount <= :maxAmount)")
    Page<Order> findOrdersByCriteria(@Param("customerId") Long customerId,
                                   @Param("status") Order.OrderStatus status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   @Param("minAmount") java.math.BigDecimal minAmount,
                                   @Param("maxAmount") java.math.BigDecimal maxAmount,
                                   Pageable pageable);
    
    /**
     * Count orders by status
     * Uses Spring Data JPA query derivation
     */
    long countByStatus(Order.OrderStatus status);
    
    /**
     * Find orders that need processing (for background jobs)
     * Uses custom JPQL query for business logic
     */
    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' AND o.createdAt < :cutoffTime")
    List<Order> findOrdersNeedingProcessing(@Param("cutoffTime") LocalDateTime cutoffTime);
}
