package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.api.PaymentsApi;
import com.ecommerce.productorder.api.model.*;
import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import com.ecommerce.productorder.payment.service.PaymentService;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PaymentsApiImpl implements PaymentsApi {

  private final PaymentService paymentService;

  public PaymentsApiImpl(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @Override
  public ResponseEntity<PaymentResponseApi> processPayment(PaymentRequest paymentRequest) {
    log.info("Processing payment for order: {}", paymentRequest.getOrderId());
    ProcessPaymentRequest dtoRequest = new ProcessPaymentRequest();
    dtoRequest.setOrderId(paymentRequest.getOrderId());
    dtoRequest.setCustomerId(paymentRequest.getCustomerId());
    dtoRequest.setPaymentMethod(
        paymentRequest.getPaymentMethod() != null
            ? paymentRequest.getPaymentMethod().name()
            : null);
    dtoRequest.setCardNumber(paymentRequest.getCardNumber());
    dtoRequest.setCardHolderName(paymentRequest.getCardHolderName());
    dtoRequest.setExpiryDate(paymentRequest.getExpiryDate());
    dtoRequest.setCvv(paymentRequest.getCvv());
    dtoRequest.setDescription(paymentRequest.getDescription());
    dtoRequest.setCustomerEmail(paymentRequest.getCustomerEmail());
    dtoRequest.setBillingAddress(paymentRequest.getBillingAddress());
    dtoRequest.setCity(paymentRequest.getCity());
    dtoRequest.setState(paymentRequest.getState());
    dtoRequest.setPostalCode(paymentRequest.getPostalCode());
    dtoRequest.setCountry(paymentRequest.getCountry());

    var response = paymentService.processPayment(dtoRequest);
    return ResponseEntity.ok(convertToApiModel(response));
  }

  @Override
  public ResponseEntity<Object> getAllPayments(Integer page, Integer size) {
    Page<PaymentResponse> paymentsPage = paymentService.getAllPayments(PageRequest.of(page, size));
    return ResponseEntity.ok(paymentsPage);
  }

  @Override
  public ResponseEntity<PaymentResponseApi> getPaymentById(String paymentId) {
    return paymentService
        .getPaymentById(paymentId)
        .map(this::convertToApiModel)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.status(404).body(null));
  }

  @Override
  public ResponseEntity<MessageResponse> deletePayment(String paymentId) {
    log.info("Cancelling payment: {}", paymentId);
    paymentService.cancelPayment(paymentId);
    return ResponseEntity.ok(
        new MessageResponse().message("Payment cancelled successfully").success(true));
  }

  @Override
  public ResponseEntity<PaymentResponseApi> getPaymentByOrderId(Long orderId) {
    return paymentService
        .getPaymentByOrderId(orderId)
        .map(this::convertToApiModel)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.status(404).body(null));
  }

  @Override
  public ResponseEntity<List<PaymentResponseApi>> getPaymentsByCustomer(Long customerId) {
    var payments = paymentService.getPaymentsByCustomerId(customerId);
    return ResponseEntity.ok(
        payments.stream().map(this::convertToApiModel).collect(Collectors.toList()));
  }

  @Override
  public ResponseEntity<PaymentResponseApi> refundPayment(String paymentId) {
    log.info("Refunding payment: {}", paymentId);
    var response = paymentService.refundPayment(paymentId, null);
    return ResponseEntity.ok(convertToApiModel(response));
  }

  @Override
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PaymentStatistics> getPaymentStatistics() {
    var stats = (Map<String, Object>) paymentService.getPaymentStatistics();
    var apiStats = new PaymentStatistics();
    apiStats.setTotalPayments(
        stats.get("totalPayments") != null
            ? ((Number) stats.get("totalPayments")).longValue()
            : 0L);
    apiStats.setSuccessfulPayments(
        stats.get("successfulPayments") != null
            ? ((Number) stats.get("successfulPayments")).longValue()
            : 0L);
    apiStats.setFailedPayments(
        stats.get("failedPayments") != null
            ? ((Number) stats.get("failedPayments")).longValue()
            : 0L);
    apiStats.setTotalAmount(
        stats.get("totalAmount") != null ? (BigDecimal) stats.get("totalAmount") : BigDecimal.ZERO);
    return ResponseEntity.ok(apiStats);
  }

  private PaymentResponseApi convertToApiModel(PaymentResponse dto) {
    var apiModel = new PaymentResponseApi();
    apiModel.setId(dto.id());
    apiModel.setPaymentId(dto.paymentId());
    apiModel.setOrderId(dto.orderId());
    apiModel.setCustomerId(dto.customerId());
    apiModel.setAmount(dto.amount());
    apiModel.setStatus(dto.status().name());
    apiModel.setPaymentMethod(dto.paymentMethod().name());
    apiModel.setTransactionId(dto.transactionId());
    apiModel.setGatewayResponse(dto.gatewayResponse());
    apiModel.setFailureReason(dto.failureReason());
    apiModel.setProcessedAt(
        dto.processedAt() != null ? dto.processedAt().atOffset(ZoneOffset.UTC) : null);
    apiModel.setCreatedAt(
        dto.createdAt() != null ? dto.createdAt().atOffset(ZoneOffset.UTC) : null);
    apiModel.setUpdatedAt(
        dto.updatedAt() != null ? dto.updatedAt().atOffset(ZoneOffset.UTC) : null);
    return apiModel;
  }
}
