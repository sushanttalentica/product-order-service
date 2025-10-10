package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.dto.response.MessageResponse;
import com.ecommerce.productorder.payment.dto.request.ProcessPaymentRequest;
import com.ecommerce.productorder.payment.dto.response.PaymentResponse;
import com.ecommerce.productorder.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing and management operations")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Process payment", description = "Process a payment for an order (Customer only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Payment processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Customer role required")
    })
    public ResponseEntity<PaymentResponse> processPayment(
            @Parameter(description = "Payment processing data") @Valid @RequestBody ProcessPaymentRequest request) {
        log.info("Processing payment for order ID: {}", request.getOrderId());
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get payment by ID", description = "Retrieve payment details by payment ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<?> getPaymentById(@PathVariable String paymentId) {
        log.debug("Retrieving payment with ID: {}", paymentId);
        return paymentService.getPaymentById(paymentId)
                .map(payment -> ResponseEntity.ok((Object) payment))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(MessageResponse.error("Payment not found with ID: " + paymentId)));
    }
    
    
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get payment by order ID", description = "Retrieve payment details by order ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<?> getPaymentByOrderId(@PathVariable Long orderId) {
        log.debug("Retrieving payment for order ID: {}", orderId);
        return paymentService.getPaymentByOrderId(orderId)
                .map(payment -> ResponseEntity.ok((Object) payment))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(MessageResponse.error("Payment not found for order ID: " + orderId)));
    }
    
    
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get payments by customer ID", description = "Retrieve all payments for a specific customer")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<PaymentResponse>> getPaymentsByCustomerId(@PathVariable Long customerId) {
        log.debug("Retrieving payments for customer ID: {}", customerId);
        List<PaymentResponse> response = paymentService.getPaymentsByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund payment", description = "Refund a payment (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment refunded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid refund data"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<PaymentResponse> refundPayment(
            @PathVariable String paymentId,
            @Parameter(description = "Refund amount") @RequestParam String refundAmount) {
        log.info("Refunding payment with ID: {} for amount: {}", paymentId, refundAmount);
        PaymentResponse response = paymentService.refundPayment(paymentId, refundAmount);
        return ResponseEntity.ok(response);
    }
    
    
    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel payment", description = "Cancel a payment (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Payment not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable String paymentId) {
        log.info("Cancelling payment with ID: {}", paymentId);
        PaymentResponse response = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all payments", description = "Retrieve all payments with pagination (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(@PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving all payments with pagination: {}", pageable);
        Page<PaymentResponse> response = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get payment statistics", description = "Retrieve payment statistics (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Object> getPaymentStatistics() {
        log.debug("Retrieving payment statistics");
        Object statistics = paymentService.getPaymentStatistics();
        return ResponseEntity.ok(statistics);
    }
}
