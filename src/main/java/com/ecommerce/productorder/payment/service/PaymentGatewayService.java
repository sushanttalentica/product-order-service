package com.ecommerce.productorder.payment.service;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;

import java.math.BigDecimal;

/**
 * Service interface for Payment Gateway operations
 * 
 * Design Principles Applied:
 * - Interface Segregation: Defines only necessary gateway operations
 * - Single Responsibility: Only handles payment gateway communication
 * - Dependency Inversion: Depends on abstractions, not implementations
 * - Strategy Pattern: Allows different gateway implementations
 * - Adapter Pattern: Adapts external gateway APIs to internal interfaces
 * - Business Logic Encapsulation: Encapsulates gateway-specific logic
 */
public interface PaymentGatewayService {
    
    /**
     * Processes payment through external gateway
     * Handles payment authorization and processing
     * 
     * @param payment the payment entity
     * @param request the payment processing request
     * @return PaymentResponse containing gateway response
     * @throws IllegalArgumentException if request is invalid
     * @throws RuntimeException if gateway communication fails
     */
    PaymentResponse processPayment(Payment payment, ProcessPaymentRequest request);
    
    /**
     * Processes refund through external gateway
     * Handles payment refund processing
     * 
     * @param payment the payment entity
     * @param refundAmount the amount to refund
     * @return PaymentResponse containing gateway response
     * @throws IllegalArgumentException if refund amount is invalid
     * @throws RuntimeException if gateway communication fails
     */
    PaymentResponse processRefund(Payment payment, BigDecimal refundAmount);
    
    /**
     * Validates payment method
     * Validates payment method and card details
     * 
     * @param request the payment processing request
     * @return true if payment method is valid, false otherwise
     */
    boolean validatePaymentMethod(ProcessPaymentRequest request);
    
    /**
     * Gets supported payment methods
     * Returns list of supported payment methods
     * 
     * @return List of supported payment methods
     */
    java.util.List<String> getSupportedPaymentMethods();
    
    /**
     * Checks gateway health
     * Verifies gateway communication
     * 
     * @return true if gateway is healthy, false otherwise
     */
    boolean isGatewayHealthy();
}
