package com.ecommerce.productorder.payment.grpc;

import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import com.ecommerce.productorder.payment.service.PaymentService;
import com.ecommerce.productorder.payment.grpc.PaymentProto.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Payment gRPC Service Tests")
class PaymentGrpcServiceTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentGrpcService paymentGrpcService;

    private ProcessPaymentRequest testRequest;
    private PaymentResponse testResponse;

    @BeforeEach
    void setUp() {
        // Arrange - Setup test data
        testRequest = ProcessPaymentRequest.builder()
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("199.98"))
                .paymentMethod("CREDIT_CARD")
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .cvv("123")
                .customerEmail("john@example.com")
                .build();

        testResponse = PaymentResponse.builder()
                .id(1L)
                .paymentId("PAY-001")
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("199.98"))
                .status("COMPLETED")
                .paymentMethod("CREDIT_CARD")
                .transactionId("TXN-001")
                .gatewayResponse("Payment authorized successfully")
                .processedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should process payment successfully via gRPC")
    void shouldProcessPaymentSuccessfullyViaGrpc() {
        // Arrange
        ProcessPaymentRequest grpcRequest = ProcessPaymentRequest.newBuilder()
                .setOrderId(1L)
                .setCustomerId(1L)
                .setAmount("199.98")
                .setPaymentMethod("CREDIT_CARD")
                .setCardNumber("4111111111111111")
                .setCardHolderName("John Doe")
                .setExpiryDate("12/25")
                .setCvv("123")
                .setCustomerEmail("john@example.com")
                .build();

        when(paymentService.processPayment(any(ProcessPaymentRequest.class))).thenReturn(testResponse);

        StreamObserver<ProcessPaymentResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.processPayment(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<ProcessPaymentResponse> responseCaptor = ArgumentCaptor.forClass(ProcessPaymentResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ProcessPaymentResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Payment processed successfully");
        assertThat(response.getPayment().getPaymentId()).isEqualTo("PAY-001");
        assertThat(response.getPayment().getStatus()).isEqualTo("COMPLETED");

        verify(paymentService).processPayment(any(ProcessPaymentRequest.class));
    }

    @Test
    @DisplayName("Should handle payment processing error via gRPC")
    void shouldHandlePaymentProcessingErrorViaGrpc() {
        // Arrange
        ProcessPaymentRequest grpcRequest = ProcessPaymentRequest.newBuilder()
                .setOrderId(1L)
                .setCustomerId(1L)
                .setAmount("199.98")
                .setPaymentMethod("CREDIT_CARD")
                .setCardNumber("4111111111111111")
                .setCardHolderName("John Doe")
                .setExpiryDate("12/25")
                .setCvv("123")
                .setCustomerEmail("john@example.com")
                .build();

        when(paymentService.processPayment(any(ProcessPaymentRequest.class)))
                .thenThrow(new RuntimeException("Payment processing failed"));

        StreamObserver<ProcessPaymentResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.processPayment(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<ProcessPaymentResponse> responseCaptor = ArgumentCaptor.forClass(ProcessPaymentResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ProcessPaymentResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Payment processing failed");

        verify(paymentService).processPayment(any(ProcessPaymentRequest.class));
    }

    @Test
    @DisplayName("Should get payment by ID successfully via gRPC")
    void shouldGetPaymentByIdSuccessfullyViaGrpc() {
        // Arrange
        GetPaymentRequest grpcRequest = GetPaymentRequest.newBuilder()
                .setPaymentId("PAY-001")
                .build();

        when(paymentService.getPaymentById("PAY-001")).thenReturn(Optional.of(testResponse));

        StreamObserver<GetPaymentResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.getPayment(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<GetPaymentResponse> responseCaptor = ArgumentCaptor.forClass(GetPaymentResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        GetPaymentResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Payment retrieved successfully");
        assertThat(response.getPayment().getPaymentId()).isEqualTo("PAY-001");

        verify(paymentService).getPaymentById("PAY-001");
    }

    @Test
    @DisplayName("Should handle payment not found via gRPC")
    void shouldHandlePaymentNotFoundViaGrpc() {
        // Arrange
        GetPaymentRequest grpcRequest = GetPaymentRequest.newBuilder()
                .setPaymentId("PAY-001")
                .build();

        when(paymentService.getPaymentById("PAY-001")).thenReturn(Optional.empty());

        StreamObserver<GetPaymentResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.getPayment(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<GetPaymentResponse> responseCaptor = ArgumentCaptor.forClass(GetPaymentResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        GetPaymentResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Payment not found");

        verify(paymentService).getPaymentById("PAY-001");
    }

    @Test
    @DisplayName("Should get payment by order ID successfully via gRPC")
    void shouldGetPaymentByOrderIdSuccessfullyViaGrpc() {
        // Arrange
        GetPaymentByOrderIdRequest grpcRequest = GetPaymentByOrderIdRequest.newBuilder()
                .setOrderId(1L)
                .build();

        when(paymentService.getPaymentByOrderId(1L)).thenReturn(Optional.of(testResponse));

        StreamObserver<GetPaymentByOrderIdResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.getPaymentByOrderId(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<GetPaymentByOrderIdResponse> responseCaptor = ArgumentCaptor.forClass(GetPaymentByOrderIdResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        GetPaymentByOrderIdResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Payment retrieved successfully");
        assertThat(response.getPayment().getOrderId()).isEqualTo(1L);

        verify(paymentService).getPaymentByOrderId(1L);
    }

    @Test
    @DisplayName("Should handle payment not found by order ID via gRPC")
    void shouldHandlePaymentNotFoundByOrderIdViaGrpc() {
        // Arrange
        GetPaymentByOrderIdRequest grpcRequest = GetPaymentByOrderIdRequest.newBuilder()
                .setOrderId(1L)
                .build();

        when(paymentService.getPaymentByOrderId(1L)).thenReturn(Optional.empty());

        StreamObserver<GetPaymentByOrderIdResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.getPaymentByOrderId(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<GetPaymentByOrderIdResponse> responseCaptor = ArgumentCaptor.forClass(GetPaymentByOrderIdResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        GetPaymentByOrderIdResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Payment not found for order ID");

        verify(paymentService).getPaymentByOrderId(1L);
    }

    @Test
    @DisplayName("Should refund payment successfully via gRPC")
    void shouldRefundPaymentSuccessfullyViaGrpc() {
        // Arrange
        RefundPaymentRequest grpcRequest = RefundPaymentRequest.newBuilder()
                .setPaymentId("PAY-001")
                .setRefundAmount("100.00")
                .build();

        PaymentResponse refundResponse = PaymentResponse.builder()
                .id(1L)
                .paymentId("PAY-001")
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("199.98"))
                .status("REFUNDED")
                .paymentMethod("CREDIT_CARD")
                .transactionId("REF-001")
                .gatewayResponse("Refund processed successfully")
                .processedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentService.refundPayment("PAY-001", new BigDecimal("100.00"))).thenReturn(refundResponse);

        StreamObserver<RefundPaymentResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.refundPayment(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<RefundPaymentResponse> responseCaptor = ArgumentCaptor.forClass(RefundPaymentResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        RefundPaymentResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Payment refunded successfully");
        assertThat(response.getPayment().getStatus()).isEqualTo("REFUNDED");

        verify(paymentService).refundPayment("PAY-001", new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should handle refund payment error via gRPC")
    void shouldHandleRefundPaymentErrorViaGrpc() {
        // Arrange
        RefundPaymentRequest grpcRequest = RefundPaymentRequest.newBuilder()
                .setPaymentId("PAY-001")
                .setRefundAmount("100.00")
                .build();

        when(paymentService.refundPayment("PAY-001", new BigDecimal("100.00")))
                .thenThrow(new RuntimeException("Refund failed"));

        StreamObserver<RefundPaymentResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.refundPayment(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<RefundPaymentResponse> responseCaptor = ArgumentCaptor.forClass(RefundPaymentResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        RefundPaymentResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Refund failed");

        verify(paymentService).refundPayment("PAY-001", new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should cancel payment successfully via gRPC")
    void shouldCancelPaymentSuccessfullyViaGrpc() {
        // Arrange
        CancelPaymentRequest grpcRequest = CancelPaymentRequest.newBuilder()
                .setPaymentId("PAY-001")
                .build();

        PaymentResponse cancelResponse = PaymentResponse.builder()
                .id(1L)
                .paymentId("PAY-001")
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("199.98"))
                .status("CANCELLED")
                .paymentMethod("CREDIT_CARD")
                .processedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(paymentService.cancelPayment("PAY-001")).thenReturn(cancelResponse);

        StreamObserver<CancelPaymentResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.cancelPayment(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<CancelPaymentResponse> responseCaptor = ArgumentCaptor.forClass(CancelPaymentResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        CancelPaymentResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Payment cancelled successfully");
        assertThat(response.getPayment().getStatus()).isEqualTo("CANCELLED");

        verify(paymentService).cancelPayment("PAY-001");
    }

    @Test
    @DisplayName("Should handle cancel payment error via gRPC")
    void shouldHandleCancelPaymentErrorViaGrpc() {
        // Arrange
        CancelPaymentRequest grpcRequest = CancelPaymentRequest.newBuilder()
                .setPaymentId("PAY-001")
                .build();

        when(paymentService.cancelPayment("PAY-001"))
                .thenThrow(new RuntimeException("Cancellation failed"));

        StreamObserver<CancelPaymentResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.cancelPayment(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<CancelPaymentResponse> responseCaptor = ArgumentCaptor.forClass(CancelPaymentResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        CancelPaymentResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Cancellation failed");

        verify(paymentService).cancelPayment("PAY-001");
    }

    @Test
    @DisplayName("Should get payments by customer ID successfully via gRPC")
    void shouldGetPaymentsByCustomerIdSuccessfullyViaGrpc() {
        // Arrange
        GetPaymentsByCustomerIdRequest grpcRequest = GetPaymentsByCustomerIdRequest.newBuilder()
                .setCustomerId(1L)
                .build();

        when(paymentService.getPaymentsByCustomerId(1L)).thenReturn(List.of(testResponse));

        StreamObserver<GetPaymentsByCustomerIdResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.getPaymentsByCustomerId(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<GetPaymentsByCustomerIdResponse> responseCaptor = ArgumentCaptor.forClass(GetPaymentsByCustomerIdResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        GetPaymentsByCustomerIdResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Payments retrieved successfully");
        assertThat(response.getPaymentsCount()).isEqualTo(1);
        assertThat(response.getPayments(0).getPaymentId()).isEqualTo("PAY-001");

        verify(paymentService).getPaymentsByCustomerId(1L);
    }

    @Test
    @DisplayName("Should handle get payments by customer ID error via gRPC")
    void shouldHandleGetPaymentsByCustomerIdErrorViaGrpc() {
        // Arrange
        GetPaymentsByCustomerIdRequest grpcRequest = GetPaymentsByCustomerIdRequest.newBuilder()
                .setCustomerId(1L)
                .build();

        when(paymentService.getPaymentsByCustomerId(1L))
                .thenThrow(new RuntimeException("Failed to retrieve payments"));

        StreamObserver<GetPaymentsByCustomerIdResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.getPaymentsByCustomerId(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<GetPaymentsByCustomerIdResponse> responseCaptor = ArgumentCaptor.forClass(GetPaymentsByCustomerIdResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        GetPaymentsByCustomerIdResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Failed to retrieve payments");

        verify(paymentService).getPaymentsByCustomerId(1L);
    }

    @Test
    @DisplayName("Should handle health check successfully via gRPC")
    void shouldHandleHealthCheckSuccessfullyViaGrpc() {
        // Arrange
        HealthCheckRequest grpcRequest = HealthCheckRequest.newBuilder()
                .setService("payment-service")
                .build();

        StreamObserver<HealthCheckResponse> responseObserver = mock(StreamObserver.class);

        // Act
        paymentGrpcService.healthCheck(grpcRequest, responseObserver);

        // Assert
        ArgumentCaptor<HealthCheckResponse> responseCaptor = ArgumentCaptor.forClass(HealthCheckResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        HealthCheckResponse response = responseCaptor.getValue();
        assertThat(response.getHealthy()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Payment service is healthy");
    }
}
