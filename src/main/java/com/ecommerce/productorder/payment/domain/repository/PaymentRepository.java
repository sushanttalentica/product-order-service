package com.ecommerce.productorder.payment.domain.repository;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity
 * 
 * Design Principles Applied:
 * - Repository Pattern: Encapsulates data access logic
 * - Single Responsibility: Only handles Payment data access
 * - Interface Segregation: Provides only necessary methods
 * - Spring Data JPA: Leverages Spring Data JPA for automatic implementation
 * - Query Methods: Uses Spring Data query method naming conventions
 * - Custom Queries: Uses @Query for complex queries
 * - Optional Return Types: Uses Optional for null-safe operations
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Finds payment by payment ID
     * Uses Optional for null-safe operations
     * 
     * @param paymentId the payment ID to search for
     * @return Optional containing payment if found, empty otherwise
     */
    Optional<Payment> findByPaymentId(String paymentId);
    
    /**
     * Finds payment by order ID
     * Uses Optional for null-safe operations
     * 
     * @param orderId the order ID to search for
     * @return Optional containing payment if found, empty otherwise
     */
    Optional<Payment> findByOrderId(Long orderId);
    
    /**
     * Finds payments by customer ID
     * Returns list of payments for a specific customer
     * 
     * @param customerId the customer ID to search for
     * @return List of payments for the customer
     */
    List<Payment> findByCustomerId(Long customerId);
    
    /**
     * Finds payments by status
     * Returns list of payments with specific status
     * 
     * @param status the payment status to search for
     * @return List of payments with the specified status
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    /**
     * Finds payments by customer ID and status
     * Combines multiple criteria for filtering
     * 
     * @param customerId the customer ID to search for
     * @param status the payment status to search for
     * @return List of payments matching both criteria
     */
    List<Payment> findByCustomerIdAndStatus(Long customerId, Payment.PaymentStatus status);
    
    /**
     * Finds payments created between specified dates
     * Uses custom query for date range filtering
     * 
     * @param startDate the start date for the range
     * @param endDate the end date for the range
     * @return List of payments created within the date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Finds payments by amount range
     * Uses custom query for amount filtering
     * 
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return List of payments within the amount range
     */
    @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :minAmount AND :maxAmount")
    List<Payment> findPaymentsByAmountRange(@Param("minAmount") java.math.BigDecimal minAmount, 
                                           @Param("maxAmount") java.math.BigDecimal maxAmount);
    
    /**
     * Counts payments by status
     * Uses custom query for counting with conditions
     * 
     * @param status the payment status to count
     * @return Number of payments with the specified status
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") Payment.PaymentStatus status);
    
    /**
     * Counts payments by status string
     * Uses custom query for counting with string status
     * 
     * @param status the payment status string to count
     * @return Number of payments with the specified status
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") String status);
    
    /**
     * Finds payments that need processing
     * Uses custom query for business logic filtering
     * 
     * @return List of payments that are pending or processing
     */
    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'PROCESSING')")
    List<Payment> findPaymentsNeedingProcessing();
    
    /**
     * Finds failed payments for retry
     * Uses custom query for business logic filtering
     * 
     * @param retryAfter the time after which payments can be retried
     * @return List of failed payments that can be retried
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.updatedAt < :retryAfter")
    List<Payment> findFailedPaymentsForRetry(@Param("retryAfter") LocalDateTime retryAfter);
}
