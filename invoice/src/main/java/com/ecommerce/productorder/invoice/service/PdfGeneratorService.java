package com.ecommerce.productorder.invoice.service;

import com.ecommerce.productorder.domain.entity.Order;

/**
 * PDF-specific implementation of InvoiceGeneratorService. This interface provides PDF-specific
 * functionality while maintaining the generic InvoiceGeneratorService contract.
 */
public interface PdfGeneratorService extends InvoiceGeneratorService {

  byte[] generateInvoicePdf(Order order);

  byte[] generateReceiptPdf(Order order);

  byte[] generateShippingLabelPdf(Order order);
}
