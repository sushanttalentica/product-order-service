package com.ecommerce.productorder.invoice.service;

import com.ecommerce.productorder.domain.entity.Order;


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
