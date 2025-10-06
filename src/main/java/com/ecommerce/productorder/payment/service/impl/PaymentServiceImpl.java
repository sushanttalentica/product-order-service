package com.ecommerce.productorder.payment.service.impl;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.repository.OrderRepository;
import com.ecommerce.productorder.payment.domain.repository.PaymentRepository;
import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import com.ecommerce.productorder.payment.service.PaymentService;
import com.ecommerce.productorder.payment.service.PaymentGatewayService;
import com.ecommerce.productorder.payment.service.PaymentEventPublisher;
import com.ecommerce.productorder.exception.BusinessException;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of PaymentService
 * 
 * Design Principles Applied:
 * - Service Layer Pattern: Encapsulates business logic
 * - Single Responsibility: Only handles payment business logic
 * - Dependency Injection: Uses constructor injection for dependencies
 * - Transaction Management: Uses @Transactional for data consistency
 * - Exception Handling: Proper exception handling with custom exceptions
 * - Logging: Uses SLF4J for comprehensive logging
 * - Stream API: Uses Java Streams for data processing
 * - Optional: Uses Optional for null-safe operations
 * - Builder Pattern: Uses Builder pattern for object creation
 * - Factory Pattern: Uses static factory methods
 * - Command Query Separation: Separates read and write operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentEventPublisher paymentEventPublisher;
    
    /**
     * Processes a payment request
     * Handles payment authorization and processing with comprehensive error handling
     * 
     * @param request the payment processing request
     * @return PaymentResponse containing payment details
     * @throws BusinessException if payment processing fails
     */
    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        log.info("Processing payment for order ID: {}", request.getOrderId());
        
        try {
            // Validate request
            validatePaymentRequest(request);
            
            // Check for existing payment
            Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());
            if (existingPayment.isPresent()) {
                throw new BusinessException("Payment already exists for order ID: " + request.getOrderId());
            }
            
            // Create payment entity
            Payment payment = createPaymentEntity(request);
            
            // Save payment
            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment created with ID: {}", savedPayment.getPaymentId());
            
            // Process payment through gateway
            PaymentResponse gatewayResponse = processPaymentThroughGateway(savedPayment, request);
            
            // Update payment status based on gateway response
            updatePaymentStatus(savedPayment, gatewayResponse);
            
            // Publish payment event
            paymentEventPublisher.publishPaymentProcessedEvent(savedPayment);
            
            log.info("Payment processed successfully for order ID: {}", request.getOrderId());
            return PaymentResponse.fromEntity(savedPayment);
            
        } catch (Exception e) {
            log.error("Error processing payment for order ID: {}", request.getOrderId(), e);
            throw new BusinessException("Failed to process payment: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves payment by payment ID
     * Uses Optional for null-safe operations
     * 
     * @param paymentId the payment ID to search for
     * @return Optional containing payment if found, empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentById(String paymentId) {
        log.debug("Retrieving payment by ID: {}", paymentId);
        
        return paymentRepository.findByPaymentId(paymentId)
                .map(PaymentResponse::fromEntity);
    }
    
    /**
     * Retrieves payment by order ID
     * Uses Optional for null-safe operations
     * 
     * @param orderId the order ID to search for
     * @return Optional containing payment if found, empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentByOrderId(Long orderId) {
        log.debug("Retrieving payment by order ID: {}", orderId);
        
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentResponse::fromEntity);
    }
    
    /**
     * Retrieves payments by customer ID
     * Uses Java Streams for data processing
     * 
     * @param customerId the customer ID to search for
     * @return List of payments for the customer
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByCustomerId(Long customerId) {
        log.debug("Retrieving payments by customer ID: {}", customerId);
        
        return paymentRepository.findByCustomerId(customerId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves payments by status
     * Uses Java Streams for data processing
     * 
     * @param status the payment status to search for
     * @return List of payments with the specified status
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(String status) {
        log.debug("Retrieving payments by status: {}", status);
        
        try {
            Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status.toUpperCase());
            return paymentRepository.findByStatus(paymentStatus)
                    .stream()
                    .map(PaymentResponse::fromEntity)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment status: {}", status, e);
            throw new BusinessException("Invalid payment status: " + status);
        }
    }
    
    /**
     * Refunds a payment
     * Handles payment refund processing with validation
     * 
     * @param paymentId the payment ID to refund
     * @param refundAmount the amount to refund
     * @return PaymentResponse containing updated payment details
     * @throws BusinessException if refund fails
     */
    @Override
    @Transactional
    public PaymentResponse refundPayment(String paymentId, String refundAmount) {
        log.info("Processing refund for payment ID: {}, amount: {}", paymentId, refundAmount);
        
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        // Convert string refund amount to BigDecimal
        java.math.BigDecimal refundAmountDecimal = new java.math.BigDecimal(refundAmount);
        
        try {
            // Validate refund amount
            if (refundAmountDecimal.compareTo(payment.getAmount()) > 0) {
                throw new BusinessException("Refund amount cannot exceed payment amount");
            }
            
            // Process refund through gateway
            PaymentResponse gatewayResponse = paymentGatewayService.processRefund(payment, refundAmountDecimal);
            
            // Update payment status
            payment.refundPayment(refundAmountDecimal);
            Payment updatedPayment = paymentRepository.save(payment);
            
            // Publish refund event
            paymentEventPublisher.publishPaymentRefundedEvent(updatedPayment);
            
            log.info("Refund processed successfully for payment ID: {}", paymentId);
            return PaymentResponse.fromEntity(updatedPayment);
            
        } catch (Exception e) {
            log.error("Error processing refund for payment ID: {}", paymentId, e);
            throw new BusinessException("Failed to process refund: " + e.getMessage());
        }
    }
    
    /**
     * Cancels a payment
     * Handles payment cancellation with validation
     * 
     * @param paymentId the payment ID to cancel
     * @return PaymentResponse containing updated payment details
     * @throws BusinessException if cancellation fails
     */
    @Override
    @Transactional
    public PaymentResponse cancelPayment(String paymentId) {
        log.info("Cancelling payment ID: {}", paymentId);
        
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        try {
            // Validate cancellation
            if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
                throw new BusinessException("Payment cannot be cancelled in current state: " + payment.getStatus());
            }
            
            // Update payment status
            payment.setStatus(Payment.PaymentStatus.CANCELLED);
            Payment updatedPayment = paymentRepository.save(payment);
            
            // Publish cancellation event
            paymentEventPublisher.publishPaymentCancelledEvent(updatedPayment);
            
            log.info("Payment cancelled successfully for payment ID: {}", paymentId);
            return PaymentResponse.fromEntity(updatedPayment);
            
        } catch (Exception e) {
            log.error("Error cancelling payment ID: {}", paymentId, e);
            throw new BusinessException("Failed to cancel payment: " + e.getMessage());
        }
    }
    
    /**
     * Retrieves payments that need processing
     * Uses Java Streams for data processing
     * 
     * @return List of payments that need processing
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsNeedingProcessing() {
        log.debug("Retrieving payments that need processing");
        
        return paymentRepository.findPaymentsNeedingProcessing()
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Retries failed payments
     * Handles retry logic for failed payments
     * 
     * @param paymentId the payment ID to retry
     * @return PaymentResponse containing updated payment details
     * @throws BusinessException if retry fails
     */
    @Override
    @Transactional
    public PaymentResponse retryPayment(String paymentId) {
        log.info("Retrying payment ID: {}", paymentId);
        
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        try {
            // Validate retry
            if (payment.getStatus() != Payment.PaymentStatus.FAILED) {
                throw new BusinessException("Payment cannot be retried in current state: " + payment.getStatus());
            }
            
            // Reset payment status
            payment.setStatus(Payment.PaymentStatus.PENDING);
            payment.setFailureReason(null);
            Payment updatedPayment = paymentRepository.save(payment);
            
            // Publish retry event
            paymentEventPublisher.publishPaymentRetryEvent(updatedPayment);
            
            log.info("Payment retry initiated for payment ID: {}", paymentId);
            return PaymentResponse.fromEntity(updatedPayment);
            
        } catch (Exception e) {
            log.error("Error retrying payment ID: {}", paymentId, e);
            throw new BusinessException("Failed to retry payment: " + e.getMessage());
        }
    }
    
    /**
     * Validates payment request
     * Encapsulates validation logic
     * 
     * @param request the payment request to validate
     * @throws BusinessException if validation fails
     */
    private void validatePaymentRequest(ProcessPaymentRequest request) {
        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        // Optional: verify ownership
        if (request.getCustomerId() != null && !request.getCustomerId().equals(order.getCustomerId())) {
            throw new BusinessException("Customer does not match the order's customer");
        }

        // Business amount limits applied on order total instead of client-sent amount
        java.math.BigDecimal orderTotal = order.getTotalAmount();
        if (orderTotal.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Order total must be greater than zero");
        }
        if (orderTotal.compareTo(new java.math.BigDecimal("10000")) > 0) {
            throw new BusinessException("Order total exceeds maximum limit");
        }
    }
    
    /**
     * Creates payment entity from request
     * Uses Builder pattern for object creation
     * 
     * @param request the payment request
     * @return Payment entity
     */
    private Payment createPaymentEntity(ProcessPaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        return Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .amount(order.getTotalAmount())
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
                .build();
    }
    
    /**
     * Processes payment through gateway
     * Delegates to payment gateway service
     * 
     * @param payment the payment entity
     * @param request the payment request
     * @return PaymentResponse from gateway
     */
    private PaymentResponse processPaymentThroughGateway(Payment payment, ProcessPaymentRequest request) {
        return paymentGatewayService.processPayment(payment, request);
    }
    
    /**
     * Updates payment status based on gateway response
     * Encapsulates status update logic
     * 
     * @param payment the payment entity
     * @param gatewayResponse the gateway response
     */
    private void updatePaymentStatus(Payment payment, PaymentResponse gatewayResponse) {
        if ("COMPLETED".equals(gatewayResponse.getStatus())) {
            payment.processPayment(gatewayResponse.getTransactionId(), gatewayResponse.getGatewayResponse());
        } else if ("FAILED".equals(gatewayResponse.getStatus())) {
            payment.failPayment(gatewayResponse.getFailureReason());
        }
        
        paymentRepository.save(payment);
    }
    
    @Override
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        log.debug("Retrieving all payments with pagination: {}", pageable);
        return paymentRepository.findAll(pageable)
                .map(this::mapToResponse);
    }
    
    @Override
    public Object getPaymentStatistics() {
        log.debug("Retrieving payment statistics");
        
        long totalPayments = paymentRepository.count();
        long completedPayments = paymentRepository.countByStatus("COMPLETED");
        long failedPayments = paymentRepository.countByStatus("FAILED");
        long pendingPayments = paymentRepository.countByStatus("PENDING");
        long refundedPayments = paymentRepository.countByStatus("REFUNDED");
        
        return Map.of(
            "totalPayments", totalPayments,
            "completedPayments", completedPayments,
            "failedPayments", failedPayments,
            "pendingPayments", pendingPayments,
            "refundedPayments", refundedPayments,
            "successRate", totalPayments > 0 ? (double) completedPayments / totalPayments * 100 : 0
        );
    }
    
    /**
     * Maps Payment entity to PaymentResponse
     * Encapsulates mapping logic
     * 
     * @param payment the payment entity
     * @return PaymentResponse
     */
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(payment.getTransactionId())
                .gatewayResponse(payment.getGatewayResponse())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
