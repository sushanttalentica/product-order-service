package com.ecommerce.productorder.payment.service;

import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Payment operations
 * 
 * Design Principles Applied:
 * - Interface Segregation: Defines only necessary payment operations
 * - Single Responsibility: Only handles payment business logic
 * - Dependency Inversion: Depends on abstractions, not implementations
 * - Command Query Separation: Separates read and write operations
 * - Optional Return Types: Uses Optional for null-safe operations
 * - Business Logic Encapsulation: Encapsulates payment business rules
 */
public interface PaymentService {
    
    /**
     * Processes a payment request
     * Handles payment authorization and processing
     * 
     * @param request the payment processing request
     * @return PaymentResponse containing payment details
     * @throws IllegalArgumentException if request is invalid
     * @throws IllegalStateException if payment cannot be processed
     */
    PaymentResponse processPayment(ProcessPaymentRequest request);
    
    /**
     * Retrieves payment by payment ID
     * Uses Optional for null-safe operations
     * 
     * @param paymentId the payment ID to search for
     * @return Optional containing payment if found, empty otherwise
     */
    Optional<PaymentResponse> getPaymentById(String paymentId);
    
    /**
     * Retrieves payment by order ID
     * Uses Optional for null-safe operations
     * 
     * @param orderId the order ID to search for
     * @return Optional containing payment if found, empty otherwise
     */
    Optional<PaymentResponse> getPaymentByOrderId(Long orderId);
    
    /**
     * Retrieves payments by customer ID
     * Returns list of payments for a specific customer
     * 
     * @param customerId the customer ID to search for
     * @return List of payments for the customer
     */
    List<PaymentResponse> getPaymentsByCustomerId(Long customerId);
    
    /**
     * Retrieves payments by status
     * Returns list of payments with specific status
     * 
     * @param status the payment status to search for
     * @return List of payments with the specified status
     */
    List<PaymentResponse> getPaymentsByStatus(String status);
    
    /**
     * Refunds a payment
     * Handles payment refund processing
     * 
     * @param paymentId the payment ID to refund
     * @param refundAmount the amount to refund
     * @return PaymentResponse containing updated payment details
     * @throws IllegalArgumentException if refund amount is invalid
     * @throws IllegalStateException if payment cannot be refunded
     */
    PaymentResponse refundPayment(String paymentId, String refundAmount);
    
    /**
     * Cancels a payment
     * Handles payment cancellation
     * 
     * @param paymentId the payment ID to cancel
     * @return PaymentResponse containing updated payment details
     * @throws IllegalStateException if payment cannot be cancelled
     */
    PaymentResponse cancelPayment(String paymentId);
    
    /**
     * Retrieves payments that need processing
     * Returns list of payments that are pending or processing
     * 
     * @return List of payments that need processing
     */
    List<PaymentResponse> getPaymentsNeedingProcessing();
    
    /**
     * Retries failed payments
     * Handles retry logic for failed payments
     * 
     * @param paymentId the payment ID to retry
     * @return PaymentResponse containing updated payment details
     * @throws IllegalStateException if payment cannot be retried
     */
    PaymentResponse retryPayment(String paymentId);
    
    /**
     * Retrieves all payments with pagination
     * Returns paginated list of all payments
     * 
     * @param pageable pagination parameters
     * @return Page of payments
     */
    Page<PaymentResponse> getAllPayments(Pageable pageable);
    
    /**
     * Gets payment statistics
     * Returns various payment statistics
     * 
     * @return Object containing payment statistics
     */
    Object getPaymentStatistics();
}
