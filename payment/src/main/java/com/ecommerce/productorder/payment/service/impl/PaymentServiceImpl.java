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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentEventPublisher paymentEventPublisher;    
    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository, PaymentGatewayService paymentGatewayService, PaymentEventPublisher paymentEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.paymentEventPublisher = paymentEventPublisher;
    }
    

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
    

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentById(String paymentId) {
        log.debug("Retrieving payment by ID: {}", paymentId);
        
        return paymentRepository.findByPaymentId(paymentId)
                .map(PaymentResponse::fromEntity);
    }
    

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentByOrderId(Long orderId) {
        log.debug("Retrieving payment by order ID: {}", orderId);
        
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentResponse::fromEntity);
    }
    

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByCustomerId(Long customerId) {
        log.debug("Retrieving payments by customer ID: {}", customerId);
        
        return paymentRepository.findByCustomerId(customerId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    

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
    

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsNeedingProcessing() {
        log.debug("Retrieving payments that need processing");
        
        return paymentRepository.findPaymentsNeedingProcessing()
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }
    

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
        if (orderTotal.compareTo(new java.math.BigDecimal("100000000")) > 0) {
            throw new BusinessException("Order total exceeds maximum limit");
        }
    }
    

    private Payment createPaymentEntity(ProcessPaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));

        return Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .amount(order.getTotalAmount())
                .refundedAmount(BigDecimal.ZERO)
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
                .build();
    }
    

    private PaymentResponse processPaymentThroughGateway(Payment payment, ProcessPaymentRequest request) {
        return paymentGatewayService.processPayment(payment, request);
    }
    

    private void updatePaymentStatus(Payment payment, PaymentResponse gatewayResponse) {
        if ("COMPLETED".equals(gatewayResponse.status())) {
            payment.processPayment(gatewayResponse.transactionId(), gatewayResponse.gatewayResponse());
            
            // Update order status to PAYMENT_COMPLETED when payment succeeds
            updateOrderStatusAfterPayment(payment.getOrderId(), true);
        } else if ("FAILED".equals(gatewayResponse.status())) {
            payment.failPayment(gatewayResponse.failureReason());
            
            // Update order status to PAYMENT_FAILED when payment fails
            updateOrderStatusAfterPayment(payment.getOrderId(), false);
        }
        
        paymentRepository.save(payment);
    }
    

    private void updateOrderStatusAfterPayment(Long orderId, boolean paymentSuccessful) {
        try {
            orderRepository.findById(orderId).ifPresent(order -> {
                if (paymentSuccessful) {
                    com.ecommerce.productorder.domain.entity.Order.OrderStatus currentStatus = order.getStatus();
                    log.info("Processing payment success for order {} with current status: {}", orderId, currentStatus);
                    
                    // Handle status transition based on current state
                    if (currentStatus == com.ecommerce.productorder.domain.entity.Order.OrderStatus.PENDING) {
                        // PENDING → CONFIRMED (payment received)
                        order.updateStatus(com.ecommerce.productorder.domain.entity.Order.OrderStatus.CONFIRMED);
                        log.info("Order {} status updated: PENDING → CONFIRMED after successful payment", orderId);
                    } else if (currentStatus == com.ecommerce.productorder.domain.entity.Order.OrderStatus.CONFIRMED) {
                        // CONFIRMED → PROCESSING (payment confirmed, start processing)
                        order.updateStatus(com.ecommerce.productorder.domain.entity.Order.OrderStatus.PROCESSING);
                        log.info("Order {} status updated: CONFIRMED → PROCESSING after successful payment", orderId);
                    } else {
                        log.warn("Order {} is in {} status, no automatic update after payment", orderId, currentStatus);
                    }
                    
                    orderRepository.save(order);
                } else {
                    // Payment failed - log but don't change order status
                    log.warn("Payment failed for order {}, order remains in {} status", orderId, order.getStatus());
                }
            });
        } catch (Exception e) {
            log.error("Error updating order status for order ID: {} - {}", orderId, e.getMessage(), e);
            // Don't throw exception - payment is already processed
        }
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
    

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getPaymentMethod().name(),
                payment.getTransactionId(),
                payment.getGatewayResponse(),
                payment.getFailureReason(),
                payment.getProcessedAt(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
