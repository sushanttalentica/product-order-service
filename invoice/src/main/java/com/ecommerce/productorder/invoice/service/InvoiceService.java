package com.ecommerce.productorder.invoice.service;

import com.ecommerce.productorder.domain.entity.Order;

import java.util.Optional;

public interface InvoiceService {

    Optional<String> generateInvoice(Order order);

    Optional<String> getInvoiceUrl(Long orderId);

    boolean deleteInvoice(Long orderId);

    boolean invoiceExists(Long orderId);
}
