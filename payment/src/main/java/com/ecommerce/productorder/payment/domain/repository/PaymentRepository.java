package com.ecommerce.productorder.payment.domain.repository;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  Optional<Payment> findByPaymentId(String paymentId);

  Optional<Payment> findByOrderId(Long orderId);

  List<Payment> findByCustomerId(Long customerId);

  List<Payment> findByStatus(Payment.PaymentStatus status);

  List<Payment> findByCustomerIdAndStatus(Long customerId, Payment.PaymentStatus status);

  @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
  List<Payment> findPaymentsByDateRange(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :minAmount AND :maxAmount")
  List<Payment> findPaymentsByAmountRange(
      @Param("minAmount") java.math.BigDecimal minAmount,
      @Param("maxAmount") java.math.BigDecimal maxAmount);

  @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
  Long countByStatus(@Param("status") Payment.PaymentStatus status);

  @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
  Long countByStatus(@Param("status") String status);

  @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'PROCESSING')")
  List<Payment> findPaymentsNeedingProcessing();

  @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.updatedAt < :retryAfter")
  List<Payment> findFailedPaymentsForRetry(@Param("retryAfter") LocalDateTime retryAfter);
}
