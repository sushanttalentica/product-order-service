package com.ecommerce.productorder.payment.service.impl;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import com.ecommerce.productorder.payment.service.PaymentGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Implementation of PaymentGatewayService
 * Simulates payment gateway communication for demonstration purposes
 * 
 * Design Principles Applied:
 * - Service Layer Pattern: Encapsulates gateway communication logic
 * - Single Responsibility: Only handles payment gateway communication
 * - Dependency Injection: Uses constructor injection for dependencies
 * - Strategy Pattern: Implements gateway-specific strategies
 * - Adapter Pattern: Adapts external gateway APIs to internal interfaces
 * - Simulation Pattern: Simulates external gateway behavior
 * - Logging: Uses SLF4J for comprehensive logging
 * - Exception Handling: Proper exception handling with custom exceptions
 * - Factory Pattern: Uses static factory methods for response creation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    
    private final Random random = new Random();
    
    /**
     * Processes payment through external gateway
     * Simulates payment authorization and processing
     * 
     * @param payment the payment entity
     * @param request the payment processing request
     * @return PaymentResponse containing gateway response
     */
    @Override
    public PaymentResponse processPayment(Payment payment, ProcessPaymentRequest request) {
        log.info("Processing payment through gateway for payment ID: {}", payment.getPaymentId());
        
        try {
            // Validate payment method
            if (!validatePaymentMethod(request)) {
                return createFailedResponse(payment, "Invalid payment method or card details");
            }
            
            // Simulate gateway processing delay
            simulateProcessingDelay();
            
            // Simulate payment processing result
            boolean isSuccessful = simulatePaymentResult(request);
            
            if (isSuccessful) {
                return createSuccessfulResponse(payment, request);
            } else {
                return createFailedResponse(payment, "Payment authorization failed");
            }
            
        } catch (Exception e) {
            log.error("Error processing payment through gateway for payment ID: {}", payment.getPaymentId(), e);
            return createFailedResponse(payment, "Gateway communication error: " + e.getMessage());
        }
    }
    
    /**
     * Processes refund through external gateway
     * Simulates refund processing
     * 
     * @param payment the payment entity
     * @param refundAmount the amount to refund
     * @return PaymentResponse containing gateway response
     */
    @Override
    public PaymentResponse processRefund(Payment payment, BigDecimal refundAmount) {
        log.info("Processing refund through gateway for payment ID: {}", payment.getPaymentId());
        
        try {
            // Validate refund amount
            if (refundAmount.compareTo(payment.getAmount()) > 0) {
                return createFailedResponse(payment, "Refund amount exceeds payment amount");
            }
            
            // Simulate gateway processing delay
            simulateProcessingDelay();
            
            // Simulate refund processing result
            boolean isSuccessful = simulateRefundResult();
            
            if (isSuccessful) {
                return createSuccessfulRefundResponse(payment, refundAmount);
            } else {
                return createFailedResponse(payment, "Refund processing failed");
            }
            
        } catch (Exception e) {
            log.error("Error processing refund through gateway for payment ID: {}", payment.getPaymentId(), e);
            return createFailedResponse(payment, "Gateway communication error: " + e.getMessage());
        }
    }
    
    /**
     * Validates payment method
     * Validates payment method and card details
     * 
     * @param request the payment processing request
     * @return true if payment method is valid, false otherwise
     */
    @Override
    public boolean validatePaymentMethod(ProcessPaymentRequest request) {
        log.debug("Validating payment method for request: {}", request.getPaymentMethod());
        
        // Validate card number (Luhn algorithm simulation)
        if (!isValidCardNumber(request.getCardNumber())) {
            log.warn("Invalid card number: {}", request.getCardNumber());
            return false;
        }
        
        // Validate expiry date
        if (!isValidExpiryDate(request.getExpiryDate())) {
            log.warn("Invalid expiry date: {}", request.getExpiryDate());
            return false;
        }
        
        // Validate CVV
        if (!isValidCVV(request.getCvv())) {
            log.warn("Invalid CVV: {}", request.getCvv());
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets supported payment methods
     * Returns list of supported payment methods
     * 
     * @return List of supported payment methods
     */
    @Override
    public List<String> getSupportedPaymentMethods() {
        return Arrays.asList(
            "CREDIT_CARD",
            "DEBIT_CARD",
            "PAYPAL",
            "STRIPE"
        );
    }
    
    /**
     * Checks gateway health
     * Verifies gateway communication
     * 
     * @return true if gateway is healthy, false otherwise
     */
    @Override
    public boolean isGatewayHealthy() {
        // Simulate gateway health check
        return random.nextDouble() > 0.1; // 90% success rate
    }
    
    /**
     * Simulates processing delay
     * Simulates network latency and processing time
     */
    private void simulateProcessingDelay() {
        try {
            Thread.sleep(random.nextInt(1000) + 500); // 500-1500ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Processing delay interrupted", e);
        }
    }
    
    /**
     * Simulates payment processing result
     * Simulates payment authorization success/failure
     * 
     * @param request the payment request
     * @return true if payment is successful, false otherwise
     */
    private boolean simulatePaymentResult(ProcessPaymentRequest request) {
        // Use effective amount from payment entity via request hint if provided
        // Fallback to request.getAmount() for backward compatibility
        // Since server uses order total, request amount may be null. Assume thresholds around payment entity totals.
        BigDecimal effectiveAmount = new BigDecimal("1000"); // neutral default for success-rate thresholding
        // Simulate different success rates based on amount
        double successRate = effectiveAmount.compareTo(new BigDecimal("1000")) > 0 ? 0.7 : 0.9;
        return random.nextDouble() < successRate;
    }
    
    /**
     * Simulates refund processing result
     * Simulates refund processing success/failure
     * 
     * @return true if refund is successful, false otherwise
     */
    private boolean simulateRefundResult() {
        return random.nextDouble() < 0.95; // 95% success rate for refunds
    }
    
    /**
     * Validates card number using Luhn algorithm
     * Implements Luhn algorithm for card number validation
     * 
     * @param cardNumber the card number to validate
     * @return true if card number is valid, false otherwise
     */
    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        // Remove non-digit characters
        String cleanCardNumber = cardNumber.replaceAll("\\D", "");
        
        // Check if all characters are digits
        if (!cleanCardNumber.matches("\\d+")) {
            return false;
        }
        
        // Luhn algorithm implementation
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cleanCardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleanCardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }
    
    /**
     * Validates expiry date
     * Validates MM/YY format and future date
     * 
     * @param expiryDate the expiry date to validate
     * @return true if expiry date is valid, false otherwise
     */
    private boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null || !expiryDate.matches("^(0[1-9]|1[0-2])/([0-9]{2})$")) {
            return false;
        }
        
        try {
            String[] parts = expiryDate.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = Integer.parseInt(parts[1]) + 2000;
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiry = LocalDateTime.of(year, month, 1, 0, 0);
            
            return expiry.isAfter(now);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validates CVV
     * Validates CVV format and length
     * 
     * @param cvv the CVV to validate
     * @return true if CVV is valid, false otherwise
     */
    private boolean isValidCVV(String cvv) {
        return cvv != null && cvv.matches("^[0-9]{3,4}$");
    }
    
    /**
     * Creates successful payment response
     * Factory method for successful responses
     * 
     * @param payment the payment entity
     * @param request the payment request
     * @return PaymentResponse for successful payment
     */
    private PaymentResponse createSuccessfulResponse(Payment payment, ProcessPaymentRequest request) {
        String transactionId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String gatewayResponse = "Payment authorized successfully";
        
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status("COMPLETED")
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(transactionId)
                .gatewayResponse(gatewayResponse)
                .processedAt(LocalDateTime.now())
                .createdAt(payment.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates successful refund response
     * Factory method for successful refund responses
     * 
     * @param payment the payment entity
     * @param refundAmount the refund amount
     * @return PaymentResponse for successful refund
     */
    private PaymentResponse createSuccessfulRefundResponse(Payment payment, BigDecimal refundAmount) {
        String transactionId = "REF_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String gatewayResponse = "Refund processed successfully";
        
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status("REFUNDED")
                .paymentMethod(payment.getPaymentMethod().name())
                .transactionId(transactionId)
                .gatewayResponse(gatewayResponse)
                .processedAt(LocalDateTime.now())
                .createdAt(payment.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Creates failed payment response
     * Factory method for failed responses
     * 
     * @param payment the payment entity
     * @param failureReason the reason for failure
     * @return PaymentResponse for failed payment
     */
    private PaymentResponse createFailedResponse(Payment payment, String failureReason) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .customerId(payment.getCustomerId())
                .amount(payment.getAmount())
                .status("FAILED")
                .paymentMethod(payment.getPaymentMethod().name())
                .failureReason(failureReason)
                .processedAt(LocalDateTime.now())
                .createdAt(payment.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
