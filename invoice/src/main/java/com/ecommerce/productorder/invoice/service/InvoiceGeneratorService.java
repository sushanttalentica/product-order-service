package com.ecommerce.productorder.invoice.service;

import com.ecommerce.productorder.domain.entity.Order;

/**
 * Generic invoice generator service interface. This abstraction allows for different
 * implementations (PDF, HTML, etc.)
 */
public interface InvoiceGeneratorService {

  /**
   * Generate invoice content for the given order
   *
   * @param order the order to generate invoice for
   * @return byte array containing the invoice content
   */
  byte[] generateInvoice(Order order);

  /**
   * Get the content type of the generated invoice
   *
   * @return content type string (e.g., "application/pdf", "text/html")
   */
  String getContentType();
}
