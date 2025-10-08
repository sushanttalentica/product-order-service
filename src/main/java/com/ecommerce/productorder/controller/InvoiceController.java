package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.repository.OrderRepository;
import com.ecommerce.productorder.invoice.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoices", description = "Invoice management operations")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    private final OrderRepository orderRepository;
    
    
    @PostMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Generate invoice for order", description = "Generate PDF invoice for an order and upload to S3")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Invoice generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid order or business rule violation"),
        @ApiResponse(responseCode = "404", description = "Order not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Customer or Admin role required")
    })
    public ResponseEntity<Map<String, String>> generateInvoice(
            @Parameter(description = "ID of the order to generate invoice for") @PathVariable Long orderId) {
        log.info("Generating invoice for order ID: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        Optional<String> invoiceUrl = invoiceService.generateInvoice(order);
        
        if (invoiceUrl.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("invoiceUrl", invoiceUrl.get(), "message", "Invoice generated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate invoice"));
        }
    }
    
    
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get invoice URL for order", description = "Retrieve the S3 URL for an order's invoice")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invoice URL retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Invoice not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Customer or Admin role required")
    })
    public ResponseEntity<Map<String, String>> getInvoiceUrl(
            @Parameter(description = "ID of the order to get invoice for") @PathVariable Long orderId) {
        log.debug("Retrieving invoice URL for order ID: {}", orderId);
        
        Optional<String> invoiceUrl = invoiceService.getInvoiceUrl(orderId);
        
        if (invoiceUrl.isPresent()) {
            return ResponseEntity.ok(Map.of("invoiceUrl", invoiceUrl.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    
    @GetMapping("/order/{orderId}/exists")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Check if invoice exists for order", description = "Check if an invoice has been generated for an order")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invoice existence status retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Customer or Admin role required")
    })
    public ResponseEntity<Map<String, Boolean>> checkInvoiceExists(
            @Parameter(description = "ID of the order to check") @PathVariable Long orderId) {
        log.debug("Checking invoice existence for order ID: {}", orderId);
        
        boolean exists = invoiceService.invoiceExists(orderId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
    
    
    @DeleteMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete invoice for order", description = "Delete invoice from S3 and database (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Invoice deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Invoice not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Void> deleteInvoice(
            @Parameter(description = "ID of the order to delete invoice for") @PathVariable Long orderId) {
        log.info("Deleting invoice for order ID: {}", orderId);
        
        boolean deleted = invoiceService.deleteInvoice(orderId);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
