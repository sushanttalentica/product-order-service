package com.ecommerce.productorder.payment.grpc;

import com.ecommerce.productorder.payment.service.PaymentService;
import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGrpcService extends PaymentServiceGrpc.PaymentServiceImplBase {
    
    private final PaymentService paymentService;
    

    @Override
    public void processPayment(PaymentProto.ProcessPaymentRequest request, 
                              StreamObserver<PaymentProto.ProcessPaymentResponse> responseObserver) {
        try {
            log.info("Received gRPC payment request for order: {}", request.getOrderId());
            
            // Convert gRPC request to internal DTO
            ProcessPaymentRequest internalRequest = convertToInternalRequest(request);
            
            // Process payment using internal service
            PaymentResponse internalResponse = paymentService.processPayment(internalRequest);
            
            // Convert internal response to gRPC response
            PaymentProto.ProcessPaymentResponse grpcResponse = convertToGrpcResponse(internalResponse);
            
            // Send response
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
            
            log.info("Successfully processed gRPC payment for order: {}", request.getOrderId());
            
        } catch (Exception e) {
            log.error("Error processing gRPC payment for order: {}", request.getOrderId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Payment processing failed: " + e.getMessage())
                .asRuntimeException());
        }
    }
    

    @Override
    public void getPayment(PaymentProto.GetPaymentRequest request, 
                          StreamObserver<PaymentProto.GetPaymentResponse> responseObserver) {
        try {
            log.info("Received gRPC get payment request for ID: {}", request.getPaymentId());
            
            // This would need to be implemented in PaymentService
            // For now, return a not implemented response
            responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Get payment by ID not implemented yet")
                .asRuntimeException());
                
        } catch (Exception e) {
            log.error("Error getting payment for ID: {}", request.getPaymentId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Failed to get payment: " + e.getMessage())
                .asRuntimeException());
        }
    }
    

    @Override
    public void getPaymentByOrderId(PaymentProto.GetPaymentByOrderIdRequest request, 
                                   StreamObserver<PaymentProto.GetPaymentByOrderIdResponse> responseObserver) {
        try {
            log.info("Received gRPC get payment by order ID request for order: {}", request.getOrderId());
            
            // This would need to be implemented in PaymentService
            // For now, return a not implemented response
            responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Get payment by order ID not implemented yet")
                .asRuntimeException());
                
        } catch (Exception e) {
            log.error("Error getting payment for order ID: {}", request.getOrderId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Failed to get payment: " + e.getMessage())
                .asRuntimeException());
        }
    }
    

    @Override
    public void refundPayment(PaymentProto.RefundPaymentRequest request, 
                             StreamObserver<PaymentProto.RefundPaymentResponse> responseObserver) {
        try {
            log.info("Received gRPC refund payment request for payment: {}", request.getPaymentId());
            
            // This would need to be implemented in PaymentService
            // For now, return a not implemented response
            responseObserver.onError(io.grpc.Status.UNIMPLEMENTED
                .withDescription("Refund payment not implemented yet")
                .asRuntimeException());
                
        } catch (Exception e) {
            log.error("Error refunding payment for ID: {}", request.getPaymentId(), e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                .withDescription("Failed to refund payment: " + e.getMessage())
                .asRuntimeException());
        }
    }
    

    private ProcessPaymentRequest convertToInternalRequest(PaymentProto.ProcessPaymentRequest request) {
        return ProcessPaymentRequest.builder()
            .orderId(request.getOrderId())
            .customerId(request.getCustomerId())
            .paymentMethod(request.getPaymentMethod())
            .cardNumber(request.getCardNumber())
            .cardHolderName(request.getCardHolderName())
            .expiryDate(request.getExpiryDate())
            .cvv(request.getCvv())
            .description(request.getDescription())
            .customerEmail(request.getCustomerEmail())
            .billingAddress(request.getBillingAddress())
            .city(request.getCity())
            .state(request.getState())
            .postalCode(request.getPostalCode())
            .country(request.getCountry())
            .build();
    }
    

    private PaymentProto.ProcessPaymentResponse convertToGrpcResponse(PaymentResponse response) {
        PaymentProto.PaymentData paymentData = PaymentProto.PaymentData.newBuilder()
            .setId(response.id())
            .setPaymentId(response.paymentId())
            .setOrderId(response.orderId())
            .setCustomerId(response.customerId())
            .setAmount(response.amount().toString())
            .setStatus(response.status())
            .setPaymentMethod(response.paymentMethod())
            .setTransactionId(response.transactionId())
            .setGatewayResponse(response.gatewayResponse())
            .setFailureReason(response.failureReason())
            .setProcessedAt(response.processedAt() != null ? response.processedAt().toString() : "")
            .setCreatedAt(response.createdAt() != null ? response.createdAt().toString() : "")
            .setUpdatedAt(response.updatedAt() != null ? response.updatedAt().toString() : "")
            .build();
            
        return PaymentProto.ProcessPaymentResponse.newBuilder()
            .setSuccess("COMPLETED".equals(response.status()))
            .setMessage(response.failureReason() != null ? response.failureReason() : "Payment processed successfully")
            .setPayment(paymentData)
            .build();
    }
}