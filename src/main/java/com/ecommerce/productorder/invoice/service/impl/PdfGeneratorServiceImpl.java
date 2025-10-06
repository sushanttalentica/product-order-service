package com.ecommerce.productorder.invoice.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.invoice.service.PdfGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of PdfGeneratorService
 * Generates PDF documents for invoices, receipts, and shipping labels
 * 
 * Design Principles Applied:
 * - Service Layer Pattern: Encapsulates PDF generation logic
 * - Single Responsibility: Only handles PDF generation
 * - Dependency Injection: Uses constructor injection for dependencies
 * - Exception Handling: Proper exception handling with custom exceptions
 * - Logging: Uses SLF4J for comprehensive logging
 * - Factory Pattern: Uses static factory methods for PDF creation
 * - Builder Pattern: Uses Builder pattern for PDF content creation
 * - Template Method: Uses template method pattern for PDF generation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorServiceImpl implements PdfGeneratorService {
    
    /**
     * Generates PDF invoice for order
     * Creates PDF content for invoice
     * 
     * @param order the order entity
     * @return byte array containing PDF content
     * @throws IllegalArgumentException if order is invalid
     * @throws RuntimeException if PDF generation fails
     */
    @Override
    public byte[] generateInvoicePdf(Order order) {
        log.info("Generating PDF invoice for order ID: {}", order.getId());
        
        try {
            // Validate order
            validateOrderForPdf(order);
            
            // Generate PDF content
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Create PDF content (simplified implementation)
            String pdfContent = createInvoiceContent(order);
            outputStream.write(pdfContent.getBytes());
            
            byte[] pdfBytes = outputStream.toByteArray();
            outputStream.close();
            
            log.info("PDF invoice generated successfully for order ID: {}", order.getId());
            return pdfBytes;
            
        } catch (IOException e) {
            log.error("Error generating PDF invoice for order ID: {}", order.getId(), e);
            throw new RuntimeException("Failed to generate PDF invoice: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error generating PDF invoice for order ID: {}", order.getId(), e);
            throw new RuntimeException("Failed to generate PDF invoice: " + e.getMessage());
        }
    }
    
    /**
     * Generates PDF receipt for order
     * Creates PDF content for receipt
     * 
     * @param order the order entity
     * @return byte array containing PDF content
     * @throws IllegalArgumentException if order is invalid
     * @throws RuntimeException if PDF generation fails
     */
    @Override
    public byte[] generateReceiptPdf(Order order) {
        log.info("Generating PDF receipt for order ID: {}", order.getId());
        
        try {
            // Validate order
            validateOrderForPdf(order);
            
            // Generate PDF content
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Create PDF content (simplified implementation)
            String pdfContent = createReceiptContent(order);
            outputStream.write(pdfContent.getBytes());
            outputStream.close();
            
            byte[] pdfBytes = outputStream.toByteArray();
            
            log.info("PDF receipt generated successfully for order ID: {}", order.getId());
            return pdfBytes;
            
        } catch (IOException e) {
            log.error("Error generating PDF receipt for order ID: {}", order.getId(), e);
            throw new RuntimeException("Failed to generate PDF receipt: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error generating PDF receipt for order ID: {}", order.getId(), e);
            throw new RuntimeException("Failed to generate PDF receipt: " + e.getMessage());
        }
    }
    
    /**
     * Generates PDF shipping label for order
     * Creates PDF content for shipping label
     * 
     * @param order the order entity
     * @return byte array containing PDF content
     * @throws IllegalArgumentException if order is invalid
     * @throws RuntimeException if PDF generation fails
     */
    @Override
    public byte[] generateShippingLabelPdf(Order order) {
        log.info("Generating PDF shipping label for order ID: {}", order.getId());
        
        try {
            // Validate order
            validateOrderForPdf(order);
            
            // Generate PDF content
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Create PDF content (simplified implementation)
            String pdfContent = createShippingLabelContent(order);
            outputStream.write(pdfContent.getBytes());
            outputStream.close();
            
            byte[] pdfBytes = outputStream.toByteArray();
            
            log.info("PDF shipping label generated successfully for order ID: {}", order.getId());
            return pdfBytes;
            
        } catch (IOException e) {
            log.error("Error generating PDF shipping label for order ID: {}", order.getId(), e);
            throw new RuntimeException("Failed to generate PDF shipping label: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error generating PDF shipping label for order ID: {}", order.getId(), e);
            throw new RuntimeException("Failed to generate PDF shipping label: " + e.getMessage());
        }
    }
    
    /**
     * Validates order for PDF generation
     * Encapsulates validation logic
     * 
     * @param order the order to validate
     * @throws IllegalArgumentException if order is invalid
     */
    private void validateOrderForPdf(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        if (order.getId() == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        
        if (order.getCustomerEmail() == null || order.getCustomerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total amount must be greater than zero");
        }
    }
    
    /**
     * Creates invoice content
     * Factory method for invoice content creation
     * 
     * @param order the order entity
     * @return String containing invoice content
     */
    private String createInvoiceContent(Order order) {
        StringBuilder content = new StringBuilder();
        
        // Invoice header
        content.append("INVOICE\n");
        content.append("=======\n\n");
        
        // Invoice details
        content.append("Invoice Number: ").append(order.getOrderNumber()).append("\n");
        content.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        content.append("Customer ID: ").append(order.getCustomerId()).append("\n");
        content.append("Customer Email: ").append(order.getCustomerEmail()).append("\n");
        content.append("Order Status: ").append(order.getStatus()).append("\n");
        content.append("Total Amount: $").append(order.getTotalAmount()).append("\n");
        
        if (order.getShippingAddress() != null) {
            content.append("Shipping Address: ").append(order.getShippingAddress()).append("\n");
        }
        
        content.append("\n");
        content.append("Thank you for your business!\n");
        
        return content.toString();
    }
    
    /**
     * Creates receipt content
     * Factory method for receipt content creation
     * 
     * @param order the order entity
     * @return String containing receipt content
     */
    private String createReceiptContent(Order order) {
        StringBuilder content = new StringBuilder();
        
        // Receipt header
        content.append("RECEIPT\n");
        content.append("=======\n\n");
        
        // Receipt details
        content.append("Receipt Number: ").append(order.getOrderNumber()).append("\n");
        content.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        content.append("Customer ID: ").append(order.getCustomerId()).append("\n");
        content.append("Customer Email: ").append(order.getCustomerEmail()).append("\n");
        content.append("Order Status: ").append(order.getStatus()).append("\n");
        content.append("Total Amount: $").append(order.getTotalAmount()).append("\n");
        
        content.append("\n");
        content.append("Payment received. Thank you!\n");
        
        return content.toString();
    }
    
    /**
     * Creates shipping label content
     * Factory method for shipping label content creation
     * 
     * @param order the order entity
     * @return String containing shipping label content
     */
    private String createShippingLabelContent(Order order) {
        StringBuilder content = new StringBuilder();
        
        // Shipping label header
        content.append("SHIPPING LABEL\n");
        content.append("==============\n\n");
        
        // Shipping details
        content.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        content.append("Customer ID: ").append(order.getCustomerId()).append("\n");
        content.append("Customer Email: ").append(order.getCustomerEmail()).append("\n");
        
        if (order.getShippingAddress() != null) {
            content.append("Shipping Address: ").append(order.getShippingAddress()).append("\n");
        }
        
        content.append("\n");
        content.append("Handle with care!\n");
        
        return content.toString();
    }
}
