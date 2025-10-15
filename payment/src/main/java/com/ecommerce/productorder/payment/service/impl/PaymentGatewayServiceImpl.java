package com.ecommerce.productorder.payment.service.impl;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import com.ecommerce.productorder.payment.service.PaymentGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {
    
    private final Random random = new Random();
    
    public PaymentGatewayServiceImpl() {
    }
    

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
    

    @Override
    public List<String> getSupportedPaymentMethods() {
        return Arrays.asList(
            "CREDIT_CARD",
            "DEBIT_CARD",
            "PAYPAL",
            "STRIPE"
        );
    }
    

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
    

    private boolean simulatePaymentResult(ProcessPaymentRequest request) {
        // Always succeed for testing
        return true;
    }
    

    private boolean simulateRefundResult() {
        // Always succeed for testing
        return true;
    }
    

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
    

    private boolean isValidCVV(String cvv) {
        return cvv != null && cvv.matches("^[0-9]{3,4}$");
    }
    

    private PaymentResponse createSuccessfulResponse(Payment payment, ProcessPaymentRequest request) {
        String transactionId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String gatewayResponse = "Payment authorized successfully";
        
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                "COMPLETED",
                payment.getPaymentMethod().name(),
                transactionId,
                gatewayResponse,
                null,
                LocalDateTime.now(),
                payment.getCreatedAt(),
                LocalDateTime.now()
        );
    }
    

    private PaymentResponse createSuccessfulRefundResponse(Payment payment, BigDecimal refundAmount) {
        String transactionId = "REF_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String gatewayResponse = "Refund processed successfully";
        
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                "REFUNDED",
                payment.getPaymentMethod().name(),
                transactionId,
                gatewayResponse,
                null,
                LocalDateTime.now(),
                payment.getCreatedAt(),
                LocalDateTime.now()
        );
    }
    

    private PaymentResponse createFailedResponse(Payment payment, String failureReason) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getCustomerId(),
                payment.getAmount(),
                "FAILED",
                payment.getPaymentMethod().name(),
                null,
                null,
                failureReason,
                LocalDateTime.now(),
                payment.getCreatedAt(),
                LocalDateTime.now()
        );
    }
}
