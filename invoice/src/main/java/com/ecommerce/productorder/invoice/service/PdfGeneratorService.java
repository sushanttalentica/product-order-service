package com.ecommerce.productorder.invoice.service;

import com.ecommerce.productorder.domain.entity.Order;

public interface PdfGeneratorService {

  byte[] generateInvoicePdf(Order order);

  byte[] generateReceiptPdf(Order order);

  byte[] generateShippingLabelPdf(Order order);
}
