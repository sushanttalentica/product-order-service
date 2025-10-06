package com.ecommerce.productorder.payment.service.impl;

import com.ecommerce.productorder.payment.domain.entity.Payment;
import com.ecommerce.productorder.payment.domain.repository.PaymentRepository;
import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import com.ecommerce.productorder.payment.service.PaymentGatewayService;
import com.ecommerce.productorder.payment.service.PaymentEventPublisher;
import com.ecommerce.productorder.exception.BusinessException;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentServiceImpl
 * 
 * Design Principles Applied:
 * - Test-Driven Development: Comprehensive test coverage
 * - AAA Pattern: Arrange, Act, Assert
 * - Mocking: Proper use of mocks for dependencies
 * - Test Isolation: Each test is independent
 * - Descriptive Test Names: Clear test method names
 * - Edge Case Testing: Tests for boundary conditions
 * - Exception Testing: Tests for error scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Payment Service Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment testPayment;
    private ProcessPaymentRequest processRequest;

    @BeforeEach
    void setUp() {
        // Arrange - Setup test data
        testPayment = Payment.builder()
                .id(1L)
                .paymentId("PAY-001")
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("199.98"))
                .status(Payment.PaymentStatus.PENDING)
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .transactionId("TXN-001")
                .gatewayResponse("Payment authorized successfully")
                .build();

        processRequest = ProcessPaymentRequest.builder()
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
    }

    @Test
    @DisplayName("Should process payment successfully")
    void shouldProcessPaymentSuccessfully() {
        // Arrange
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(paymentGatewayService.processPayment(any(Payment.class), any(ProcessPaymentRequest.class)))
                .thenReturn(PaymentResponse.builder()
                        .status("COMPLETED")
                        .transactionId("TXN-001")
                        .gatewayResponse("Payment authorized successfully")
                        .build());

        // Act
        PaymentResponse response = paymentService.processPayment(processRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getPaymentId()).isEqualTo("PAY-001");
        verify(paymentRepository).findByOrderId(1L);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentEventPublisher).publishPaymentProcessedEvent(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when payment already exists")
    void shouldThrowExceptionWhenPaymentAlreadyExists() {
        // Arrange
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(testPayment));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(processRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Payment already exists for order ID: 1");
        
        verify(paymentRepository).findByOrderId(1L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid payment amount")
    void shouldThrowExceptionForInvalidPaymentAmount() {
        // Arrange
        ProcessPaymentRequest invalidRequest = ProcessPaymentRequest.builder()
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("-10.00")) // Invalid negative amount
                .paymentMethod("CREDIT_CARD")
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .cvv("123")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Payment amount must be greater than zero");
    }

    @Test
    @DisplayName("Should throw exception for excessive payment amount")
    void shouldThrowExceptionForExcessivePaymentAmount() {
        // Arrange
        ProcessPaymentRequest invalidRequest = ProcessPaymentRequest.builder()
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("15000.00")) // Exceeds maximum limit
                .paymentMethod("CREDIT_CARD")
                .cardNumber("4111111111111111")
                .cardHolderName("John Doe")
                .expiryDate("12/25")
                .cvv("123")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> paymentService.processPayment(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Payment amount exceeds maximum limit");
    }

    @Test
    @DisplayName("Should get payment by ID successfully")
    void shouldGetPaymentByIdSuccessfully() {
        // Arrange
        when(paymentRepository.findByPaymentId("PAY-001")).thenReturn(Optional.of(testPayment));

        // Act
        Optional<PaymentResponse> response = paymentService.getPaymentById("PAY-001");

        // Assert
        assertThat(response).isPresent();
        assertThat(response.get().getPaymentId()).isEqualTo("PAY-001");
        verify(paymentRepository).findByPaymentId("PAY-001");
    }

    @Test
    @DisplayName("Should return empty when payment not found")
    void shouldReturnEmptyWhenPaymentNotFound() {
        // Arrange
        when(paymentRepository.findByPaymentId("PAY-001")).thenReturn(Optional.empty());

        // Act
        Optional<PaymentResponse> response = paymentService.getPaymentById("PAY-001");

        // Assert
        assertThat(response).isEmpty();
        verify(paymentRepository).findByPaymentId("PAY-001");
    }

    @Test
    @DisplayName("Should get payment by order ID successfully")
    void shouldGetPaymentByOrderIdSuccessfully() {
        // Arrange
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(testPayment));

        // Act
        Optional<PaymentResponse> response = paymentService.getPaymentByOrderId(1L);

        // Assert
        assertThat(response).isPresent();
        assertThat(response.get().getOrderId()).isEqualTo(1L);
        verify(paymentRepository).findByOrderId(1L);
    }

    @Test
    @DisplayName("Should get payments by customer ID")
    void shouldGetPaymentsByCustomerId() {
        // Arrange
        when(paymentRepository.findByCustomerId(1L)).thenReturn(List.of(testPayment));

        // Act
        List<PaymentResponse> response = paymentService.getPaymentsByCustomerId(1L);

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCustomerId()).isEqualTo(1L);
        verify(paymentRepository).findByCustomerId(1L);
    }

    @Test
    @DisplayName("Should get payments by status")
    void shouldGetPaymentsByStatus() {
        // Arrange
        when(paymentRepository.findByStatus(Payment.PaymentStatus.PENDING)).thenReturn(List.of(testPayment));

        // Act
        List<PaymentResponse> response = paymentService.getPaymentsByStatus("PENDING");

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getStatus()).isEqualTo("PENDING");
        verify(paymentRepository).findByStatus(Payment.PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Should throw exception for invalid payment status")
    void shouldThrowExceptionForInvalidPaymentStatus() {
        // Act & Assert
        assertThatThrownBy(() -> paymentService.getPaymentsByStatus("INVALID_STATUS"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid payment status");
    }

    @Test
    @DisplayName("Should refund payment successfully")
    void shouldRefundPaymentSuccessfully() {
        // Arrange
        Payment completedPayment = Payment.builder()
                .id(1L)
                .paymentId("PAY-001")
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("199.98"))
                .status(Payment.PaymentStatus.COMPLETED)
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .build();

        when(paymentRepository.findByPaymentId("PAY-001")).thenReturn(Optional.of(completedPayment));
        when(paymentGatewayService.processRefund(any(Payment.class), any(BigDecimal.class)))
                .thenReturn(PaymentResponse.builder()
                        .status("REFUNDED")
                        .transactionId("REF-001")
                        .gatewayResponse("Refund processed successfully")
                        .build());
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedPayment);

        // Act
        PaymentResponse response = paymentService.refundPayment("PAY-001", new BigDecimal("100.00"));

        // Assert
        assertThat(response).isNotNull();
        verify(paymentRepository).findByPaymentId("PAY-001");
        verify(paymentGatewayService).processRefund(any(Payment.class), any(BigDecimal.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentEventPublisher).publishPaymentRefundedEvent(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when refunding non-existent payment")
    void shouldThrowExceptionWhenRefundingNonExistentPayment() {
        // Arrange
        when(paymentRepository.findByPaymentId("PAY-001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.refundPayment("PAY-001", new BigDecimal("100.00")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment not found with ID: PAY-001");
        
        verify(paymentRepository).findByPaymentId("PAY-001");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when refund amount exceeds payment amount")
    void shouldThrowExceptionWhenRefundAmountExceedsPaymentAmount() {
        // Arrange
        when(paymentRepository.findByPaymentId("PAY-001")).thenReturn(Optional.of(testPayment));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.refundPayment("PAY-001", new BigDecimal("300.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Refund amount cannot exceed payment amount");
        
        verify(paymentRepository).findByPaymentId("PAY-001");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should cancel payment successfully")
    void shouldCancelPaymentSuccessfully() {
        // Arrange
        when(paymentRepository.findByPaymentId("PAY-001")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        PaymentResponse response = paymentService.cancelPayment("PAY-001");

        // Assert
        assertThat(response).isNotNull();
        verify(paymentRepository).findByPaymentId("PAY-001");
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentEventPublisher).publishPaymentCancelledEvent(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-existent payment")
    void shouldThrowExceptionWhenCancellingNonExistentPayment() {
        // Arrange
        when(paymentRepository.findByPaymentId("PAY-001")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> paymentService.cancelPayment("PAY-001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Payment not found with ID: PAY-001");
        
        verify(paymentRepository).findByPaymentId("PAY-001");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-cancellable payment")
    void shouldThrowExceptionWhenCancellingNonCancellablePayment() {
        // Arrange
        Payment completedPayment = Payment.builder()
                .id(1L)
                .paymentId("PAY-001")
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("199.98"))
                .status(Payment.PaymentStatus.COMPLETED)
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .build();

        when(paymentRepository.findByPaymentId("PAY-001")).thenReturn(Optional.of(completedPayment));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.cancelPayment("PAY-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Payment cannot be cancelled in current state");
        
        verify(paymentRepository).findByPaymentId("PAY-001");
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should get payments needing processing")
    void shouldGetPaymentsNeedingProcessing() {
        // Arrange
        when(paymentRepository.findPaymentsNeedingProcessing()).thenReturn(List.of(testPayment));

        // Act
        List<PaymentResponse> response = paymentService.getPaymentsNeedingProcessing();

        // Assert
        assertThat(response).hasSize(1);
        verify(paymentRepository).findPaymentsNeedingProcessing();
    }

    @Test
    @DisplayName("Should retry payment successfully")
    void shouldRetryPaymentSuccessfully() {
        // Arrange
        Payment failedPayment = Payment.builder()
                .id(1L)
                .paymentId("PAY-001")
                .orderId(1L)
                .customerId(1L)
                .amount(new BigDecimal("199.98"))
                .status(Payment.PaymentStatus.FAILED)
                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                .failureReason("Gateway timeout")
                .build();

        when(paymentRepository.findByPaymentId("PAY-001")).thenReturn(Optional.of(failedPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(failedPayment);

        // Act
        PaymentResponse response = paymentService.retryPayment("PAY-001");

        // Assert
        assertThat(response).isNotNull();
        verify(paymentRepository).findByPaymentId("PAY-001");
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentEventPublisher).publishPaymentRetryEvent(any(Payment.class));
    }

    @Test
    @DisplayName("Should throw exception when retrying non-failed payment")
    void shouldThrowExceptionWhenRetryingNonFailedPayment() {
        // Arrange
        when(paymentRepository.findByPaymentId("PAY-001")).thenReturn(Optional.of(testPayment));

        // Act & Assert
        assertThatThrownBy(() -> paymentService.retryPayment("PAY-001"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Payment cannot be retried in current state");
        
        verify(paymentRepository).findByPaymentId("PAY-001");
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
