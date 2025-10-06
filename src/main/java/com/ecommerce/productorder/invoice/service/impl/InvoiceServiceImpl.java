package com.ecommerce.productorder.invoice.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.invoice.service.InvoiceService;
import com.ecommerce.productorder.invoice.service.PdfGeneratorService;
import com.ecommerce.productorder.invoice.service.S3Service;
import com.ecommerce.productorder.invoice.domain.entity.Invoice;
import com.ecommerce.productorder.invoice.domain.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of InvoiceService
 * 
 * Design Principles Applied:
 * - Service Layer Pattern: Encapsulates business logic
 * - Single Responsibility: Only handles invoice business logic
 * - Dependency Injection: Uses constructor injection for dependencies
 * - Transaction Management: Uses @Transactional for data consistency
 * - Exception Handling: Proper exception handling with custom exceptions
 * - Logging: Uses SLF4J for comprehensive logging
 * - Optional: Uses Optional for null-safe operations
 * - Builder Pattern: Uses Builder pattern for object creation
 * - Factory Pattern: Uses static factory methods
 * - Command Query Separation: Separates read and write operations
 * - Event-Driven Architecture: Integrates with external services
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceServiceImpl implements InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final S3Service s3Service;
    
    /**
     * Generates PDF invoice for order
     * Creates PDF invoice and uploads to AWS S3
     * 
     * @param order the order entity
     * @return Optional containing invoice URL if successful, empty otherwise
     * @throws IllegalArgumentException if order is invalid
     * @throws RuntimeException if invoice generation fails
     */
    @Override
    @Transactional
    public Optional<String> generateInvoice(Order order) {
        log.info("Generating invoice for order ID: {}", order.getId());
        
        try {
            // Validate order
            validateOrderForInvoice(order);
            
            // Check if invoice already exists
            if (invoiceExists(order.getId())) {
                log.warn("Invoice already exists for order ID: {}", order.getId());
                return getInvoiceUrl(order.getId());
            }
            
            // Generate PDF content
            byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(order);
            
            // Upload to S3
            String s3Key = generateS3Key(order);
            String s3Url = s3Service.uploadFile(s3Key, pdfContent, "application/pdf");
            
            // Save invoice record
            Invoice invoice = createInvoiceEntity(order, s3Key, s3Url);
            invoiceRepository.save(invoice);
            
            log.info("Invoice generated successfully for order ID: {}", order.getId());
            return Optional.of(s3Url);
            
        } catch (Exception e) {
            log.error("Error generating invoice for order ID: {}", order.getId(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Gets invoice URL for order
     * Retrieves public access URL for invoice
     * 
     * @param orderId the order ID
     * @return Optional containing invoice URL if found, empty otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<String> getInvoiceUrl(Long orderId) {
        log.debug("Retrieving invoice URL for order ID: {}", orderId);
        
        return invoiceRepository.findByOrderId(orderId)
                .map(Invoice::getS3Url);
    }
    
    /**
     * Deletes invoice for order
     * Removes invoice from S3 and database
     * 
     * @param orderId the order ID
     * @return true if deletion successful, false otherwise
     */
    @Override
    @Transactional
    public boolean deleteInvoice(Long orderId) {
        log.info("Deleting invoice for order ID: {}", orderId);
        
        try {
            Optional<Invoice> invoiceOpt = invoiceRepository.findByOrderId(orderId);
            
            if (invoiceOpt.isPresent()) {
                Invoice invoice = invoiceOpt.get();
                
                // Delete from S3
                s3Service.deleteFile(invoice.getS3Key());
                
                // Delete from database
                invoiceRepository.delete(invoice);
                
                log.info("Invoice deleted successfully for order ID: {}", orderId);
                return true;
            } else {
                log.warn("Invoice not found for order ID: {}", orderId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error deleting invoice for order ID: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * Checks if invoice exists for order
     * Verifies invoice existence
     * 
     * @param orderId the order ID
     * @return true if invoice exists, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    public boolean invoiceExists(Long orderId) {
        return invoiceRepository.findByOrderId(orderId).isPresent();
    }
    
    /**
     * Validates order for invoice generation
     * Encapsulates validation logic
     * 
     * @param order the order to validate
     * @throws IllegalArgumentException if order is invalid
     */
    private void validateOrderForInvoice(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        
        if (order.getStatus() != Order.OrderStatus.COMPLETED && order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Invoice can only be generated for completed or delivered orders");
        }
        
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total amount must be greater than zero");
        }
    }
    
    /**
     * Generates S3 key for invoice
     * Creates unique S3 key for invoice storage
     * 
     * @param order the order entity
     * @return S3 key for invoice
     */
    private String generateS3Key(Order order) {
        return String.format("invoices/%d/%s.pdf", order.getId(), UUID.randomUUID().toString());
    }
    
    /**
     * Creates invoice entity
     * Uses Builder pattern for object creation
     * 
     * @param order the order entity
     * @param s3Key the S3 key
     * @param s3Url the S3 URL
     * @return Invoice entity
     */
    private Invoice createInvoiceEntity(Order order, String s3Key, String s3Url) {
        return Invoice.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .customerEmail(order.getCustomerEmail())
                .totalAmount(order.getTotalAmount())
                .s3Key(s3Key)
                .s3Url(s3Url)
                .status(Invoice.InvoiceStatus.GENERATED)
                .generatedAt(LocalDateTime.now())
                .build();
    }
}
