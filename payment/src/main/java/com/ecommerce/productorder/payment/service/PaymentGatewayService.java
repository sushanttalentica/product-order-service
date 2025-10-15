package com.ecommerce.productorder.payment.service;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import java.math.BigDecimal;

public interface PaymentGatewayService {

  PaymentResponse processPayment(Payment payment, ProcessPaymentRequest request);

  PaymentResponse processRefund(Payment payment, BigDecimal refundAmount);

  boolean validatePaymentMethod(ProcessPaymentRequest request);

  java.util.List<String> getSupportedPaymentMethods();

  boolean isGatewayHealthy();
}
