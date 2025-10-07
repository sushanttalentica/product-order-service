package com.ecommerce.productorder.invoice.domain.repository;

import com.ecommerce.productorder.invoice.domain.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Invoice entity
 * 
 * Design Principles Applied:
 * - Repository Pattern: Encapsulates data access logic
 * - Single Responsibility: Only handles Invoice data access
 * - Interface Segregation: Provides only necessary methods
 * - Spring Data JPA: Leverages Spring Data JPA for automatic implementation
 * - Query Methods: Uses Spring Data query method naming conventions
 * - Custom Queries: Uses @Query for complex queries
 * - Optional Return Types: Uses Optional for null-safe operations
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    /**
     * Finds invoice by order ID
     * Uses Optional for null-safe operations
     * 
     * @param orderId the order ID to search for
     * @return Optional containing invoice if found, empty otherwise
     */
    Optional<Invoice> findByOrderId(Long orderId);
    
    /**
     * Finds invoices by customer ID
     * Returns list of invoices for a specific customer
     * 
     * @param customerId the customer ID to search for
     * @return List of invoices for the customer
     */
    List<Invoice> findByCustomerId(Long customerId);
    
    /**
     * Finds invoices by status
     * Returns list of invoices with specific status
     * 
     * @param status the invoice status to search for
     * @return List of invoices with the specified status
     */
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    
    /**
     * Finds invoices by customer ID and status
     * Combines multiple criteria for filtering
     * 
     * @param customerId the customer ID to search for
     * @param status the invoice status to search for
     * @return List of invoices matching both criteria
     */
    List<Invoice> findByCustomerIdAndStatus(Long customerId, Invoice.InvoiceStatus status);
    
    /**
     * Finds invoices generated between specified dates
     * Uses custom query for date range filtering
     * 
     * @param startDate the start date for the range
     * @param endDate the end date for the range
     * @return List of invoices generated within the date range
     */
    @Query("SELECT i FROM Invoice i WHERE i.generatedAt BETWEEN :startDate AND :endDate")
    List<Invoice> findInvoicesByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Finds invoices by amount range
     * Uses custom query for amount filtering
     * 
     * @param minAmount the minimum amount
     * @param maxAmount the maximum amount
     * @return List of invoices within the amount range
     */
    @Query("SELECT i FROM Invoice i WHERE i.totalAmount BETWEEN :minAmount AND :maxAmount")
    List<Invoice> findInvoicesByAmountRange(@Param("minAmount") java.math.BigDecimal minAmount, 
                                           @Param("maxAmount") java.math.BigDecimal maxAmount);
    
    /**
     * Counts invoices by status
     * Uses custom query for counting with conditions
     * 
     * @param status the invoice status to count
     * @return Number of invoices with the specified status
     */
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    Long countByStatus(@Param("status") Invoice.InvoiceStatus status);
    
    /**
     * Finds failed invoices for retry
     * Uses custom query for business logic filtering
     * 
     * @param retryAfter the time after which invoices can be retried
     * @return List of failed invoices that can be retried
     */
    @Query("SELECT i FROM Invoice i WHERE i.status = 'FAILED' AND i.updatedAt < :retryAfter")
    List<Invoice> findFailedInvoicesForRetry(@Param("retryAfter") LocalDateTime retryAfter);
    
    /**
     * Finds invoices by S3 key
     * Uses custom query for S3 key filtering
     * 
     * @param s3Key the S3 key to search for
     * @return Optional containing invoice if found, empty otherwise
     */
    Optional<Invoice> findByS3Key(String s3Key);
}
