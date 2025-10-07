package com.ecommerce.productorder.invoice.service;

import com.ecommerce.productorder.domain.entity.Order;

/**
 * Service interface for PDF generation
 * 
 * Design Principles Applied:
 * - Interface Segregation: Defines only necessary PDF generation operations
 * - Single Responsibility: Only handles PDF generation logic
 * - Dependency Inversion: Depends on abstractions, not implementations
 * - Business Logic Encapsulation: Encapsulates PDF generation logic
 */
public interface PdfGeneratorService {
    
    /**
     * Generates PDF invoice for order
     * Creates PDF content for invoice
     * 
     * @param order the order entity
     * @return byte array containing PDF content
     * @throws IllegalArgumentException if order is invalid
     * @throws RuntimeException if PDF generation fails
     */
    byte[] generateInvoicePdf(Order order);
    
    /**
     * Generates PDF receipt for order
     * Creates PDF content for receipt
     * 
     * @param order the order entity
     * @return byte array containing PDF content
     * @throws IllegalArgumentException if order is invalid
     * @throws RuntimeException if PDF generation fails
     */
    byte[] generateReceiptPdf(Order order);
    
    /**
     * Generates PDF shipping label for order
     * Creates PDF content for shipping label
     * 
     * @param order the order entity
     * @return byte array containing PDF content
     * @throws IllegalArgumentException if order is invalid
     * @throws RuntimeException if PDF generation fails
     */
    byte[] generateShippingLabelPdf(Order order);
}
