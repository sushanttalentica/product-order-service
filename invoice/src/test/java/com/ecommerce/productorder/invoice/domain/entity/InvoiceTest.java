package com.ecommerce.productorder.invoice.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// JUnit tests for Invoice entity class.
@DisplayName("Invoice Entity Tests")
public class InvoiceTest {
  private Invoice invoice;
  private final LocalDateTime now = LocalDateTime.now();
  private final BigDecimal totalAmount = new BigDecimal("999.99");

  @BeforeEach
  void setUp() {
    invoice = new Invoice();
    invoice.setId(1L);
    invoice.setOrderId(100L);
    invoice.setCustomerId(200L);
    invoice.setCustomerEmail("test@example.com");
    invoice.setTotalAmount(totalAmount);
    invoice.setObjectKey("invoices/100/test-key.pdf");
    invoice.setObjectUrl("https://example.com/invoices/100/test-key.pdf");
    invoice.setStatus(Invoice.InvoiceStatus.GENERATED);
    invoice.setGeneratedAt(now);
    invoice.setCreatedAt(now);
    invoice.setUpdatedAt(now);
  }

  @Test
  @DisplayName("Should create invoice with all parameters")
  void shouldCreateInvoiceWithAllParameters() {
    Long id = 1L;
    Long orderId = 100L;
    Long customerId = 200L;
    String customerEmail = "test@example.com";
    BigDecimal totalAmount = new BigDecimal("999.99");
    String objectKey = "invoices/100/test-key.pdf";
    String objectUrl = "https://example.com/invoices/100/test-key.pdf";
    Invoice.InvoiceStatus status = Invoice.InvoiceStatus.GENERATED;
    LocalDateTime generatedAt = LocalDateTime.now();

    Invoice invoice =
        new Invoice(
            id, orderId, customerId, customerEmail, totalAmount, objectKey, objectUrl, status,
            generatedAt, now, now);

    assertNotNull(invoice);
    assertEquals(id, invoice.getId());
    assertEquals(orderId, invoice.getOrderId());
    assertEquals(customerId, invoice.getCustomerId());
    assertEquals(customerEmail, invoice.getCustomerEmail());
    assertEquals(totalAmount, invoice.getTotalAmount());
    assertEquals(objectKey, invoice.getObjectKey());
    assertEquals(objectUrl, invoice.getObjectUrl());
    assertEquals(status, invoice.getStatus());
    assertEquals(generatedAt, invoice.getGeneratedAt());
    assertEquals(now, invoice.getCreatedAt());
    assertEquals(now, invoice.getUpdatedAt());
  }

  @Test
  @DisplayName("Should create empty invoice with default constructor")
  void shouldCreateEmptyInvoiceWithDefaultConstructor() {
    Invoice invoice = new Invoice();

    assertNotNull(invoice);
    assertNull(invoice.getId());
    assertNull(invoice.getOrderId());
    assertNull(invoice.getCustomerId());
    assertNull(invoice.getCustomerEmail());
    assertNull(invoice.getTotalAmount());
    assertNull(invoice.getObjectKey());
    assertNull(invoice.getObjectUrl());
    assertEquals(Invoice.InvoiceStatus.GENERATED, invoice.getStatus());
    assertNull(invoice.getGeneratedAt());
    assertNull(invoice.getCreatedAt());
    assertNull(invoice.getUpdatedAt());
  }

  @Test
  @DisplayName("Should allow download when status is GENERATED")
  void shouldAllowDownloadWhenStatusIsGenerated() {
    invoice.setStatus(Invoice.InvoiceStatus.GENERATED);

    boolean canDownload = invoice.canBeDownloaded();

    assertTrue(canDownload);
  }

  @Test
  @DisplayName("Should allow download when status is SENT")
  void shouldAllowDownloadWhenStatusIsSent() {
    invoice.setStatus(Invoice.InvoiceStatus.SENT);

    boolean canDownload = invoice.canBeDownloaded();

    assertTrue(canDownload);
  }

  @Test
  @DisplayName("Should not allow download when status is FAILED")
  void shouldNotAllowDownloadWhenStatusIsFailed() {
    invoice.setStatus(Invoice.InvoiceStatus.FAILED);

    boolean canDownload = invoice.canBeDownloaded();

    assertFalse(canDownload);
  }

  @Test
  @DisplayName("Should not allow download when status is DELETED")
  void shouldNotAllowDownloadWhenStatusIsDeleted() {
    invoice.setStatus(Invoice.InvoiceStatus.DELETED);

    boolean canDownload = invoice.canBeDownloaded();

    assertFalse(canDownload);
  }

  @Test
  @DisplayName("Should allow regeneration when status is GENERATED")
  void shouldAllowRegenerationWhenStatusIsGenerated() {
    invoice.setStatus(Invoice.InvoiceStatus.GENERATED);

    boolean canRegenerate = invoice.canBeRegenerated();

    assertTrue(canRegenerate);
  }

  @Test
  @DisplayName("Should allow regeneration when status is SENT")
  void shouldAllowRegenerationWhenStatusIsSent() {
    invoice.setStatus(Invoice.InvoiceStatus.SENT);

    boolean canRegenerate = invoice.canBeRegenerated();

    assertTrue(canRegenerate);
  }

  @Test
  @DisplayName("Should not allow regeneration when status is FAILED")
  void shouldNotAllowRegenerationWhenStatusIsFailed() {
    invoice.setStatus(Invoice.InvoiceStatus.FAILED);

    boolean canRegenerate = invoice.canBeRegenerated();

    assertFalse(canRegenerate);
  }

  @Test
  @DisplayName("Should mark as sent when status is GENERATED")
  void shouldMarkAsSentWhenStatusIsGenerated() {
    invoice.setStatus(Invoice.InvoiceStatus.GENERATED);

    invoice.markAsSent();

    assertEquals(Invoice.InvoiceStatus.SENT, invoice.getStatus());
  }

  @Test
  @DisplayName("Should throw exception when marking as sent from non-GENERATED status")
  void shouldThrowExceptionWhenMarkingAsSentFromNonGeneratedStatus() {
    invoice.setStatus(Invoice.InvoiceStatus.SENT);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> invoice.markAsSent());
    assertEquals("Invoice cannot be marked as sent in current state: SENT", exception.getMessage());
  }

  @Test
  @DisplayName("Should throw exception when marking as sent from FAILED status")
  void shouldThrowExceptionWhenMarkingAsSentFromFailedStatus() {
    invoice.setStatus(Invoice.InvoiceStatus.FAILED);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> invoice.markAsSent());
    assertEquals("Invoice cannot be marked as sent in current state: FAILED", exception.getMessage());
  }

  @Test
  @DisplayName("Should mark as failed")
  void shouldMarkAsFailed() {
    String failureReason = "Generation failed";

    invoice.markAsFailed(failureReason);

    assertEquals(Invoice.InvoiceStatus.FAILED, invoice.getStatus());
    assertEquals(failureReason, invoice.getFailureReason());
  }

  @Test
  @DisplayName("Should have all expected status values")
  void shouldHaveAllExpectedStatusValues() {
    Invoice.InvoiceStatus[] statuses = Invoice.InvoiceStatus.values();

    assertEquals(4, statuses.length);
    assertTrue(java.util.Arrays.asList(statuses).contains(Invoice.InvoiceStatus.GENERATED));
    assertTrue(java.util.Arrays.asList(statuses).contains(Invoice.InvoiceStatus.SENT));
    assertTrue(java.util.Arrays.asList(statuses).contains(Invoice.InvoiceStatus.FAILED));
    assertTrue(java.util.Arrays.asList(statuses).contains(Invoice.InvoiceStatus.DELETED));
  }

  @Test
  @DisplayName("Should have correct string representation")
  void shouldHaveCorrectStringRepresentation() {
    assertEquals("GENERATED", Invoice.InvoiceStatus.GENERATED.name());
    assertEquals("SENT", Invoice.InvoiceStatus.SENT.name());
    assertEquals("FAILED", Invoice.InvoiceStatus.FAILED.name());
    assertEquals("DELETED", Invoice.InvoiceStatus.DELETED.name());
  }

  @Test
  @DisplayName("Should set and get all properties correctly")
  void shouldSetAndGetAllPropertiesCorrectly() {
    Invoice testInvoice = new Invoice();
    Long id = 1L;
    Long orderId = 100L;
    Long customerId = 200L;
    String customerEmail = "test@example.com";
    BigDecimal totalAmount = new BigDecimal("999.99");
    String objectKey = "invoices/100/test-key.pdf";
    String objectUrl = "https://example.com/invoices/100/test-key.pdf";
    Invoice.InvoiceStatus status = Invoice.InvoiceStatus.GENERATED;
    LocalDateTime generatedAt = LocalDateTime.now();
    LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
    LocalDateTime updatedAt = LocalDateTime.now();

    testInvoice.setId(id);
    testInvoice.setOrderId(orderId);
    testInvoice.setCustomerId(customerId);
    testInvoice.setCustomerEmail(customerEmail);
    testInvoice.setTotalAmount(totalAmount);
    testInvoice.setObjectKey(objectKey);
    testInvoice.setObjectUrl(objectUrl);
    testInvoice.setStatus(status);
    testInvoice.setGeneratedAt(generatedAt);
    testInvoice.setCreatedAt(createdAt);
    testInvoice.setUpdatedAt(updatedAt);

    assertEquals(id, testInvoice.getId());
    assertEquals(orderId, testInvoice.getOrderId());
    assertEquals(customerId, testInvoice.getCustomerId());
    assertEquals(customerEmail, testInvoice.getCustomerEmail());
    assertEquals(totalAmount, testInvoice.getTotalAmount());
    assertEquals(objectKey, testInvoice.getObjectKey());
    assertEquals(objectUrl, testInvoice.getObjectUrl());
    assertEquals(status, testInvoice.getStatus());
    assertEquals(generatedAt, testInvoice.getGeneratedAt());
    assertEquals(createdAt, testInvoice.getCreatedAt());
    assertEquals(updatedAt, testInvoice.getUpdatedAt());
  }

  @Test
  @DisplayName("Should handle null values gracefully")
  void shouldHandleNullValuesGracefully() {
    Invoice testInvoice = new Invoice();

    assertDoesNotThrow(
        () -> {
          testInvoice.setId(null);
          testInvoice.setOrderId(null);
          testInvoice.setCustomerId(null);
          testInvoice.setCustomerEmail(null);
          testInvoice.setTotalAmount(null);
          testInvoice.setObjectKey(null);
          testInvoice.setObjectUrl(null);
          testInvoice.setStatus(null);
          testInvoice.setGeneratedAt(null);
          testInvoice.setCreatedAt(null);
          testInvoice.setUpdatedAt(null);
        });
  }

  @Test
  @DisplayName("Should handle zero amount")
  void shouldHandleZeroAmount() {
    BigDecimal zeroAmount = BigDecimal.ZERO;

    invoice.setTotalAmount(zeroAmount);

    assertEquals(zeroAmount, invoice.getTotalAmount());
  }

  @Test
  @DisplayName("Should handle large amounts")
  void shouldHandleLargeAmounts() {
    BigDecimal largeAmount = new BigDecimal("999999.99");

    invoice.setTotalAmount(largeAmount);

    assertEquals(largeAmount, invoice.getTotalAmount());
  }

  @Test
  @DisplayName("Should handle long email addresses")
  void shouldHandleLongEmailAddresses() {
    String longEmail =
        "very.long.email.address.that.might.exceed.normal.length@very.long.domain.name.com";

    invoice.setCustomerEmail(longEmail);

    assertEquals(longEmail, invoice.getCustomerEmail());
  }
}
