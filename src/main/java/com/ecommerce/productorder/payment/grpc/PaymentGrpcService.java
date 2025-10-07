package com.ecommerce.productorder.payment.grpc;

import com.ecommerce.productorder.payment.service.PaymentService;
import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * gRPC service implementation for payment operations
 * 
 * Design Principles Applied:
 * - Service Layer Pattern: Encapsulates gRPC payment operations
 * - Single Responsibility: Only handles gRPC payment requests
 * - Dependency Injection: Uses constructor injection for PaymentService
 * - Logging: Uses SLF4J for comprehensive logging
 * - Exception Handling: Proper exception handling with gRPC status codes
 * - Data Conversion: Converts between gRPC and internal DTOs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGrpcService extends PaymentServiceGrpc.PaymentServiceImplBase {
    
    private final PaymentService paymentService;
    
    /**
     * Process payment via gRPC
     * 
     * @param request gRPC payment request
     * @param responseObserver gRPC response observer
     */
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
    
    /**
     * Get payment by ID via gRPC
     * 
     * @param request gRPC get payment request
     * @param responseObserver gRPC response observer
     */
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
    
    /**
     * Get payment by order ID via gRPC
     * 
     * @param request gRPC get payment by order ID request
     * @param responseObserver gRPC response observer
     */
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
    
    /**
     * Refund payment via gRPC
     * 
     * @param request gRPC refund payment request
     * @param responseObserver gRPC response observer
     */
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
    
    /**
     * Convert gRPC request to internal DTO
     * 
     * @param request gRPC payment request
     * @return internal payment request DTO
     */
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
    
    /**
     * Convert internal response to gRPC response
     * 
     * @param response internal payment response
     * @return gRPC payment response
     */
    private PaymentProto.ProcessPaymentResponse convertToGrpcResponse(PaymentResponse response) {
        PaymentProto.PaymentData paymentData = PaymentProto.PaymentData.newBuilder()
            .setId(response.getId())
            .setPaymentId(response.getPaymentId())
            .setOrderId(response.getOrderId())
            .setCustomerId(response.getCustomerId())
            .setAmount(response.getAmount().toString())
            .setStatus(response.getStatus())
            .setPaymentMethod(response.getPaymentMethod())
            .setTransactionId(response.getTransactionId())
            .setGatewayResponse(response.getGatewayResponse())
            .setFailureReason(response.getFailureReason())
            .setProcessedAt(response.getProcessedAt() != null ? response.getProcessedAt().toString() : "")
            .setCreatedAt(response.getCreatedAt() != null ? response.getCreatedAt().toString() : "")
            .setUpdatedAt(response.getUpdatedAt() != null ? response.getUpdatedAt().toString() : "")
            .build();
            
        return PaymentProto.ProcessPaymentResponse.newBuilder()
            .setSuccess("COMPLETED".equals(response.getStatus()))
            .setMessage(response.getFailureReason() != null ? response.getFailureReason() : "Payment processed successfully")
            .setPayment(paymentData)
            .build();
    }
}