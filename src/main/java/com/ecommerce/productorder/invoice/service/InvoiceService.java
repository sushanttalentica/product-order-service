package com.ecommerce.productorder.invoice.service;

import com.ecommerce.productorder.domain.entity.Order;

import java.util.Optional;


public interface InvoiceService {
    
    /**
     * Generates PDF invoice for order
     * Creates PDF invoice and uploads to AWS S3
     * 
     * @param order the order entity
     * @return Optional containing invoice URL if successful, empty otherwise
     * @throws IllegalArgumentException if order is invalid
     * @throws RuntimeException if invoice generation fails
     */
    Optional<String> generateInvoice(Order order);
    
    /**
     * Gets invoice URL for order
     * Retrieves public access URL for invoice
     * 
     * @param orderId the order ID
     * @return Optional containing invoice URL if found, empty otherwise
     */
    Optional<String> getInvoiceUrl(Long orderId);
    
    /**
     * Deletes invoice for order
     * Removes invoice from S3 and database
     * 
     * @param orderId the order ID
     * @return true if deletion successful, false otherwise
     */
    boolean deleteInvoice(Long orderId);
    
    /**
     * Checks if invoice exists for order
     * Verifies invoice existence
     * 
     * @param orderId the order ID
     * @return true if invoice exists, false otherwise
     */
    boolean invoiceExists(Long orderId);
}
