package com.ecommerce.productorder.domain.repository;

import com.ecommerce.productorder.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByCustomerEmail(String customerEmail, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(Long customerId, Order.OrderStatus status, Pageable pageable);

    Page<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Order> findByCustomerIdAndCreatedAtBetween(Long customerId, 
                                                   LocalDateTime startDate, 
                                                   LocalDateTime endDate, 
                                                   Pageable pageable);

    Page<Order> findByTotalAmountGreaterThan(java.math.BigDecimal amount, Pageable pageable);

    Page<Order> findByTotalAmountBetween(java.math.BigDecimal minAmount, java.math.BigDecimal maxAmount, Pageable pageable);

    List<Order> findByStatusAndCreatedAtBefore(Order.OrderStatus status, LocalDateTime createdAt);

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
    

    long countByStatus(Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' AND o.createdAt < :cutoffTime")
    List<Order> findOrdersNeedingProcessing(@Param("cutoffTime") LocalDateTime cutoffTime);
}
