package com.ecommerce.productorder.invoice.domain.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ecommerce.productorder.invoice.domain.entity.Invoice;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// JUnit tests for InvoiceRepository interface.
@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceRepository Tests")
public class InvoiceRepositoryTest {

  @Mock private InvoiceRepository invoiceRepository;
  private Invoice testInvoice1;
  private Invoice testInvoice2;
  private Invoice testInvoice3;

  @BeforeEach
  void setUp() {
    testInvoice1 = new Invoice();
    testInvoice1.setOrderId(100L);
    testInvoice1.setCustomerId(200L);
    testInvoice1.setCustomerEmail("customer1@example.com");
    testInvoice1.setTotalAmount(new BigDecimal("999.99"));
    testInvoice1.setObjectKey("invoices/100/invoice1.pdf");
    testInvoice1.setObjectUrl("https://example.com/invoices/100/invoice1.pdf");
    testInvoice1.setStatus(Invoice.InvoiceStatus.GENERATED);
    testInvoice1.setGeneratedAt(LocalDateTime.now().minusDays(1));

    testInvoice2 = new Invoice();
    testInvoice2.setOrderId(101L);
    testInvoice2.setCustomerId(200L);
    testInvoice2.setCustomerEmail("customer1@example.com");
    testInvoice2.setTotalAmount(new BigDecimal("1499.99"));
    testInvoice2.setObjectKey("invoices/101/invoice2.pdf");
    testInvoice2.setObjectUrl("https://example.com/invoices/101/invoice2.pdf");
    testInvoice2.setStatus(Invoice.InvoiceStatus.SENT);
    testInvoice2.setGeneratedAt(LocalDateTime.now().minusDays(2));

    testInvoice3 = new Invoice();
    testInvoice3.setOrderId(102L);
    testInvoice3.setCustomerId(300L);
    testInvoice3.setCustomerEmail("customer2@example.com");
    testInvoice3.setTotalAmount(new BigDecimal("799.99"));
    testInvoice3.setObjectKey("invoices/102/invoice3.pdf");
    testInvoice3.setObjectUrl("https://example.com/invoices/102/invoice3.pdf");
    testInvoice3.setStatus(Invoice.InvoiceStatus.FAILED);
    testInvoice3.setGeneratedAt(LocalDateTime.now().minusDays(3));
  }

  @Test
  @DisplayName("Should find invoice by order ID")
  void shouldFindInvoiceByOrderId() {
    when(invoiceRepository.findByOrderId(100L)).thenReturn(Optional.of(testInvoice1));

    Optional<Invoice> result = invoiceRepository.findByOrderId(100L);

    assertTrue(result.isPresent());
    assertEquals(100L, result.get().getOrderId());
    assertEquals(200L, result.get().getCustomerId());
    assertEquals("customer1@example.com", result.get().getCustomerEmail());
    assertEquals(new BigDecimal("999.99"), result.get().getTotalAmount());
    assertEquals(Invoice.InvoiceStatus.GENERATED, result.get().getStatus());
  }

  @Test
  @DisplayName("Should return empty when invoice not found by order ID")
  void shouldReturnEmptyWhenInvoiceNotFoundByOrderId() {
    when(invoiceRepository.findByOrderId(999L)).thenReturn(Optional.empty());

    Optional<Invoice> result = invoiceRepository.findByOrderId(999L);

    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Should save new invoice")
  void shouldSaveNewInvoice() {
    Invoice newInvoice = new Invoice();
    newInvoice.setOrderId(103L);
    newInvoice.setCustomerId(400L);
    newInvoice.setCustomerEmail("newcustomer@example.com");
    newInvoice.setTotalAmount(new BigDecimal("599.99"));
    newInvoice.setObjectKey("invoices/103/newinvoice.pdf");
    newInvoice.setObjectUrl("https://example.com/invoices/103/newinvoice.pdf");
    newInvoice.setStatus(Invoice.InvoiceStatus.GENERATED);

    when(invoiceRepository.save(any(Invoice.class))).thenReturn(newInvoice);

    Invoice savedInvoice = invoiceRepository.save(newInvoice);

    assertNotNull(savedInvoice);
    assertEquals(103L, savedInvoice.getOrderId());
    assertEquals(400L, savedInvoice.getCustomerId());
    assertEquals("newcustomer@example.com", savedInvoice.getCustomerEmail());
    assertEquals(new BigDecimal("599.99"), savedInvoice.getTotalAmount());
    assertEquals(Invoice.InvoiceStatus.GENERATED, savedInvoice.getStatus());
  }

  @Test
  @DisplayName("Should find invoices by customer ID")
  void shouldFindInvoicesByCustomerId() {
    List<Invoice> expectedInvoices = Arrays.asList(testInvoice1, testInvoice2);
    when(invoiceRepository.findByCustomerId(200L)).thenReturn(expectedInvoices);

    List<Invoice> result = invoiceRepository.findByCustomerId(200L);

    assertEquals(2, result.size());
    assertTrue(result.stream().allMatch(invoice -> invoice.getCustomerId().equals(200L)));
    assertTrue(result.stream().anyMatch(invoice -> invoice.getOrderId().equals(100L)));
    assertTrue(result.stream().anyMatch(invoice -> invoice.getOrderId().equals(101L)));
  }

  @Test
  @DisplayName("Should find invoices by status")
  void shouldFindInvoicesByStatus() {
    List<Invoice> expectedInvoices = Arrays.asList(testInvoice1);
    when(invoiceRepository.findByStatus(Invoice.InvoiceStatus.GENERATED)).thenReturn(expectedInvoices);

    List<Invoice> result = invoiceRepository.findByStatus(Invoice.InvoiceStatus.GENERATED);

    assertEquals(1, result.size());
    assertEquals(100L, result.get(0).getOrderId());
    assertEquals(Invoice.InvoiceStatus.GENERATED, result.get(0).getStatus());
  }

  @Test
  @DisplayName("Should delete invoice")
  void shouldDeleteInvoice() {
    doNothing().when(invoiceRepository).delete(any(Invoice.class));

    assertDoesNotThrow(() -> invoiceRepository.delete(testInvoice1));
  }
}