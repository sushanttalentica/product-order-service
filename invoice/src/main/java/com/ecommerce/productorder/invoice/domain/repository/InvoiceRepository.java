package com.ecommerce.productorder.invoice.domain.repository;

import com.ecommerce.productorder.invoice.domain.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByOrderId(Long orderId);

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findByStatus(Invoice.InvoiceStatus status);

    List<Invoice> findByCustomerIdAndStatus(Long customerId, Invoice.InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.generatedAt BETWEEN :startDate AND :endDate")
    List<Invoice> findInvoicesByDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    

    @Query("SELECT i FROM Invoice i WHERE i.totalAmount BETWEEN :minAmount AND :maxAmount")
    List<Invoice> findInvoicesByAmountRange(@Param("minAmount") java.math.BigDecimal minAmount, 
                                           @Param("maxAmount") java.math.BigDecimal maxAmount);
    

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    Long countByStatus(@Param("status") Invoice.InvoiceStatus status);
    

    @Query("SELECT i FROM Invoice i WHERE i.status = 'FAILED' AND i.updatedAt < :retryAfter")
    List<Invoice> findFailedInvoicesForRetry(@Param("retryAfter") LocalDateTime retryAfter);

    Optional<Invoice> findByS3Key(String s3Key);
}
