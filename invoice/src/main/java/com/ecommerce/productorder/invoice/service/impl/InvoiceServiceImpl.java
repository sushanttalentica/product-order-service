package com.ecommerce.productorder.invoice.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.invoice.domain.entity.Invoice;
import com.ecommerce.productorder.invoice.domain.repository.InvoiceRepository;
import com.ecommerce.productorder.invoice.service.InvoiceGeneratorService;
import com.ecommerce.productorder.invoice.service.InvoiceService;
import com.ecommerce.productorder.invoice.service.ObjectStoreService;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class InvoiceServiceImpl implements InvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final InvoiceGeneratorService invoiceGeneratorService;
  private final ObjectStoreService objectStoreService;

  public InvoiceServiceImpl(
      InvoiceRepository invoiceRepository,
      InvoiceGeneratorService invoiceGeneratorService,
      ObjectStoreService objectStoreService) {
    this.invoiceRepository = invoiceRepository;
    this.invoiceGeneratorService = invoiceGeneratorService;
    this.objectStoreService = objectStoreService;
  }

  @Override
  @Transactional
  public Optional<String> generateInvoice(Order order) {
    // Validate order first
    validateOrderForInvoice(order);
    
    log.info("Generating invoice for order ID: {}", order.getId());

    try {
      // Check if invoice already exists
      if (invoiceExists(order.getId())) {
        log.warn("Invoice already exists for order ID: {}", order.getId());
        return getInvoiceUrl(order.getId());
      }

      // Generate invoice content
      byte[] content = invoiceGeneratorService.generateInvoice(order);

      // Upload to object store
      String objectKey = generateObjectKey(order);
      String objectUrl =
          objectStoreService.uploadFile(
              objectKey, content, invoiceGeneratorService.getContentType());

      // Save invoice record
      Invoice invoice = createInvoiceEntity(order, objectKey, objectUrl);
      invoiceRepository.save(invoice);

      log.info("Invoice generated successfully for order ID: {}", order.getId());
      return Optional.of(objectUrl);

    } catch (Exception e) {
      log.error("Error generating invoice for order ID: {}", order.getId(), e);
      return Optional.empty();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> getInvoiceUrl(Long orderId) {
    log.debug("Retrieving invoice URL for order ID: {}", orderId);
    
    if (orderId == null) {
      throw new IllegalArgumentException("Order ID cannot be null");
    }

    return invoiceRepository.findByOrderId(orderId).map(Invoice::getObjectUrl);
  }

  @Override
  @Transactional
  public boolean deleteInvoice(Long orderId) {
    log.info("Deleting invoice for order ID: {}", orderId);

    if (orderId == null) {
      throw new IllegalArgumentException("Order ID cannot be null");
    }

    try {
      Optional<Invoice> invoiceOpt = invoiceRepository.findByOrderId(orderId);

      if (invoiceOpt.isPresent()) {
        Invoice invoice = invoiceOpt.get();

        // Delete from object store
        objectStoreService.deleteFile(invoice.getObjectKey());

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

  @Override
  @Transactional(readOnly = true)
  public boolean invoiceExists(Long orderId) {
    if (orderId == null) {
      throw new IllegalArgumentException("Order ID cannot be null");
    }
    return invoiceRepository.findByOrderId(orderId).isPresent();
  }

  private void validateOrderForInvoice(Order order) {
    if (order == null) {
      throw new IllegalArgumentException("Order cannot be null");
    }

    if (order.getStatus() != Order.OrderStatus.COMPLETED
        && order.getStatus() != Order.OrderStatus.DELIVERED) {
      throw new IllegalArgumentException(
          "Invoice can only be generated for completed or delivered orders");
    }

    if (order.getTotalAmount() == null
        || order.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Order total amount must be greater than zero");
    }
  }

  private String generateObjectKey(Order order) {
    return String.format("invoices/%d/%s.pdf", order.getId(), UUID.randomUUID().toString());
  }

  private Invoice createInvoiceEntity(Order order, String objectKey, String objectUrl) {
    Invoice invoice = new Invoice();
    invoice.setOrderId(order.getId());
    invoice.setCustomerId(order.getCustomerId());
    invoice.setCustomerEmail(order.getCustomerEmail());
    invoice.setTotalAmount(order.getTotalAmount());
    invoice.setObjectKey(objectKey);
    invoice.setObjectUrl(objectUrl);
    invoice.setStatus(Invoice.InvoiceStatus.GENERATED);
    invoice.setGeneratedAt(LocalDateTime.now());
    return invoice;
  }
}
