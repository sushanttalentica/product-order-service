package com.ecommerce.productorder.payment.service;

import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    

    PaymentResponse processPayment(ProcessPaymentRequest request);
    

    Optional<PaymentResponse> getPaymentById(String paymentId);
    

    Optional<PaymentResponse> getPaymentByOrderId(Long orderId);
    

    List<PaymentResponse> getPaymentsByCustomerId(Long customerId);
    

    List<PaymentResponse> getPaymentsByStatus(String status);
    

    PaymentResponse refundPayment(String paymentId, String refundAmount);
    

    PaymentResponse cancelPayment(String paymentId);
    

    List<PaymentResponse> getPaymentsNeedingProcessing();
    

    PaymentResponse retryPayment(String paymentId);
    

    Page<PaymentResponse> getAllPayments(Pageable pageable);
    

    Object getPaymentStatistics();
}
