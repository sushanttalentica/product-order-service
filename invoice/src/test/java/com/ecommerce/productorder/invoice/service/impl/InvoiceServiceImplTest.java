package com.ecommerce.productorder.invoice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.invoice.domain.entity.Invoice;
import com.ecommerce.productorder.invoice.domain.repository.InvoiceRepository;
import com.ecommerce.productorder.invoice.service.InvoiceGeneratorService;
import com.ecommerce.productorder.invoice.service.ObjectStoreService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// JUnit tests for InvoiceServiceImpl class.
@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceServiceImpl Tests")
public class InvoiceServiceImplTest {
  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceGeneratorService invoiceGeneratorService;
  @Mock private ObjectStoreService objectStoreService;
  @InjectMocks private InvoiceServiceImpl invoiceService;
  private Order testOrder;
  private Invoice testInvoice;
  private final String testObjectKey = "invoices/100/test-key.pdf";
  private final String testObjectUrl = "https://example.com/invoices/100/test-key.pdf";
  private final byte[] testContent = "test invoice content".getBytes();

  @BeforeEach
  void setUp() {
    testOrder = new Order();
    testOrder.setId(100L);
    testOrder.setOrderNumber(UUID.randomUUID().toString());
    testOrder.setCustomerId(200L);
    testOrder.setCustomerEmail("test@example.com");
    testOrder.setStatus(Order.OrderStatus.DELIVERED);
    testOrder.setTotalAmount(new BigDecimal("999.99"));
    testOrder.setShippingAddress("123 Test St, Test City, TC 12345");
    testOrder.setCreatedAt(LocalDateTime.now());
    testOrder.setUpdatedAt(LocalDateTime.now());

    testInvoice = new Invoice();
    testInvoice.setId(1L);
    testInvoice.setOrderId(100L);
    testInvoice.setCustomerId(200L);
    testInvoice.setCustomerEmail("test@example.com");
    testInvoice.setTotalAmount(new BigDecimal("999.99"));
    testInvoice.setObjectKey(testObjectKey);
    testInvoice.setObjectUrl(testObjectUrl);
    testInvoice.setStatus(Invoice.InvoiceStatus.GENERATED);
    testInvoice.setGeneratedAt(LocalDateTime.now());
    testInvoice.setCreatedAt(LocalDateTime.now());
    testInvoice.setUpdatedAt(LocalDateTime.now());

    lenient().when(invoiceGeneratorService.generateInvoice(any(Order.class))).thenReturn(testContent);
    lenient().when(invoiceGeneratorService.getContentType()).thenReturn("application/pdf");
    lenient().when(objectStoreService.uploadFile(anyString(), any(byte[].class), anyString())).thenReturn(testObjectUrl);
    lenient().when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
    lenient().when(invoiceRepository.findByOrderId(anyLong())).thenReturn(Optional.empty());
  }

  @Test
  @DisplayName("Should generate invoice successfully for new order")
  void shouldGenerateInvoiceSuccessfullyForNewOrder() {
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.empty());
    when(objectStoreService.uploadFile(anyString(), any(byte[].class), anyString())).thenReturn(testObjectUrl);
    when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

    Optional<String> result = invoiceService.generateInvoice(testOrder);

    assertTrue(result.isPresent());
    assertEquals(testObjectUrl, result.get());
    verify(invoiceGeneratorService, times(1)).generateInvoice(testOrder);
    verify(objectStoreService, times(1)).uploadFile(anyString(), any(byte[].class), anyString());
    verify(invoiceRepository, times(1)).save(any(Invoice.class));
  }

  @Test
  @DisplayName("Should return existing invoice URL when invoice already exists")
  void shouldReturnExistingInvoiceUrlWhenInvoiceAlreadyExists() {
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.of(testInvoice));

    Optional<String> result = invoiceService.generateInvoice(testOrder);

    assertTrue(result.isPresent());
    assertEquals(testObjectUrl, result.get());
    verify(invoiceGeneratorService, never()).generateInvoice(any(Order.class));
    verify(objectStoreService, never()).uploadFile(anyString(), any(byte[].class), anyString());
    verify(invoiceRepository, never()).save(any(Invoice.class));
  }

  @Test
  @DisplayName("Should throw exception when order is null")
  void shouldThrowExceptionWhenOrderIsNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.generateInvoice(null);
    });
  }

  @Test
  @DisplayName("Should throw exception when order ID is null")
  void shouldThrowExceptionWhenOrderIdIsNull() {
    testOrder.setId(null);
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.generateInvoice(testOrder);
    });
  }

  @Test
  @DisplayName("Should throw exception when customer email is null")
  void shouldThrowExceptionWhenCustomerEmailIsNull() {
    testOrder.setCustomerEmail(null);
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.generateInvoice(testOrder);
    });
  }

  @Test
  @DisplayName("Should throw exception when customer email is empty")
  void shouldThrowExceptionWhenCustomerEmailIsEmpty() {
    testOrder.setCustomerEmail("");
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.generateInvoice(testOrder);
    });
  }

  @Test
  @DisplayName("Should throw exception when total amount is null")
  void shouldThrowExceptionWhenTotalAmountIsNull() {
    testOrder.setTotalAmount(null);
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.generateInvoice(testOrder);
    });
  }

  @Test
  @DisplayName("Should throw exception when total amount is zero or negative")
  void shouldThrowExceptionWhenTotalAmountIsZeroOrNegative() {
    testOrder.setTotalAmount(BigDecimal.ZERO);
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.generateInvoice(testOrder);
    });
  }

  @Test
  @DisplayName("Should throw exception when order items are null")
  void shouldThrowExceptionWhenOrderItemsAreNull() {
    testOrder.setOrderItems(null);
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.generateInvoice(testOrder);
    });
  }

  @Test
  @DisplayName("Should throw exception when order items are empty")
  void shouldThrowExceptionWhenOrderItemsAreEmpty() {
    testOrder.setOrderItems(java.util.Collections.emptyList());
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.generateInvoice(testOrder);
    });
  }

  @Test
  @DisplayName("Should handle invoice generation failure gracefully")
  void shouldHandleInvoiceGenerationFailureGracefully() {
    when(invoiceGeneratorService.generateInvoice(testOrder)).thenThrow(new RuntimeException("Generation failed"));

    assertThrows(RuntimeException.class, () -> {
      invoiceService.generateInvoice(testOrder);
    });
  }

  @Test
  @DisplayName("Should handle object store upload failure gracefully")
  void shouldHandleObjectStoreUploadFailureGracefully() {
    when(objectStoreService.uploadFile(anyString(), any(byte[].class), anyString())).thenThrow(new RuntimeException("Upload failed"));

    assertThrows(RuntimeException.class, () -> {
      invoiceService.generateInvoice(testOrder);
    });
  }

  @Test
  @DisplayName("Should get invoice URL successfully")
  void shouldGetInvoiceUrlSuccessfully() {
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.of(testInvoice));

    Optional<String> result = invoiceService.getInvoiceUrl(testOrder.getId());

    assertTrue(result.isPresent());
    assertEquals(testObjectUrl, result.get());
    verify(invoiceRepository, times(1)).findByOrderId(testOrder.getId());
  }

  @Test
  @DisplayName("Should return empty when invoice not found for URL retrieval")
  void shouldReturnEmptyWhenInvoiceNotFoundForUrlRetrieval() {
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.empty());

    Optional<String> result = invoiceService.getInvoiceUrl(testOrder.getId());

    assertFalse(result.isPresent());
    verify(invoiceRepository, times(1)).findByOrderId(testOrder.getId());
  }

  @Test
  @DisplayName("Should throw exception when order ID is null for URL retrieval")
  void shouldThrowExceptionWhenOrderIdIsNullForUrlRetrieval() {
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.getInvoiceUrl(null);
    });
  }

  @Test
  @DisplayName("Should delete invoice successfully")
  void shouldDeleteInvoiceSuccessfully() {
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.of(testInvoice));
    when(objectStoreService.deleteFile(testObjectKey)).thenReturn(true);
    doNothing().when(invoiceRepository).delete(testInvoice);

    boolean result = invoiceService.deleteInvoice(testOrder.getId());

    assertTrue(result);
    verify(invoiceRepository, times(1)).findByOrderId(testOrder.getId());
    verify(objectStoreService, times(1)).deleteFile(testObjectKey);
    verify(invoiceRepository, times(1)).delete(testInvoice);
  }

  @Test
  @DisplayName("Should return false when invoice not found for deletion")
  void shouldReturnFalseWhenInvoiceNotFoundForDeletion() {
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.empty());

    boolean result = invoiceService.deleteInvoice(testOrder.getId());

    assertFalse(result);
    verify(invoiceRepository, times(1)).findByOrderId(testOrder.getId());
    verify(objectStoreService, never()).deleteFile(anyString());
    verify(invoiceRepository, never()).delete(any(Invoice.class));
  }

  @Test
  @DisplayName("Should handle object store deletion failure gracefully")
  void shouldHandleObjectStoreDeletionFailureGracefully() {
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.of(testInvoice));
    when(objectStoreService.deleteFile(testObjectKey)).thenReturn(false);

    boolean result = invoiceService.deleteInvoice(testOrder.getId());

    assertFalse(result);
    verify(invoiceRepository, times(1)).findByOrderId(testOrder.getId());
    verify(objectStoreService, times(1)).deleteFile(testObjectKey);
    verify(invoiceRepository, never()).delete(any(Invoice.class));
  }

  @Test
  @DisplayName("Should throw exception when order ID is null for deletion")
  void shouldThrowExceptionWhenOrderIdIsNullForDeletion() {
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.deleteInvoice(null);
    });
  }

  @Test
  @DisplayName("Should check if invoice exists successfully")
  void shouldCheckIfInvoiceExistsSuccessfully() {
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.of(testInvoice));

    boolean result = invoiceService.invoiceExists(testOrder.getId());

    assertTrue(result);
    verify(invoiceRepository, times(1)).findByOrderId(testOrder.getId());
  }

  @Test
  @DisplayName("Should return false when invoice does not exist")
  void shouldReturnFalseWhenInvoiceDoesNotExist() {
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.empty());

    boolean result = invoiceService.invoiceExists(testOrder.getId());

    assertFalse(result);
    verify(invoiceRepository, times(1)).findByOrderId(testOrder.getId());
  }

  @Test
  @DisplayName("Should throw exception when order ID is null for existence check")
  void shouldThrowExceptionWhenOrderIdIsNullForExistenceCheck() {
    assertThrows(IllegalArgumentException.class, () -> {
      invoiceService.invoiceExists(null);
    });
  }

  @Test
  @DisplayName("Should handle complete invoice lifecycle")
  void shouldHandleCompleteInvoiceLifecycle() {
    // Generate invoice
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.empty());
    when(objectStoreService.uploadFile(anyString(), any(byte[].class), anyString())).thenReturn(testObjectUrl);
    when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

    Optional<String> generateResult = invoiceService.generateInvoice(testOrder);
    assertTrue(generateResult.isPresent());
    assertEquals(testObjectUrl, generateResult.get());

    // Check if invoice exists
    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.of(testInvoice));
    boolean existsResult = invoiceService.invoiceExists(testOrder.getId());
    assertTrue(existsResult);

    // Get invoice URL
    Optional<String> urlResult = invoiceService.getInvoiceUrl(testOrder.getId());
    assertTrue(urlResult.isPresent());
    assertEquals(testObjectUrl, urlResult.get());

    // Delete invoice
    when(objectStoreService.deleteFile(testObjectKey)).thenReturn(true);
    doNothing().when(invoiceRepository).delete(testInvoice);
    boolean deleteResult = invoiceService.deleteInvoice(testOrder.getId());
    assertTrue(deleteResult);
  }

  @Test
  @DisplayName("Should handle order with very large total amount")
  void shouldHandleOrderWithVeryLargeTotalAmount() {
    testOrder.setTotalAmount(new BigDecimal("999999.99"));

    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.empty());
    when(objectStoreService.uploadFile(anyString(), any(byte[].class), anyString())).thenReturn(testObjectUrl);
    when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

    Optional<String> result = invoiceService.generateInvoice(testOrder);

    assertTrue(result.isPresent());
    assertEquals(testObjectUrl, result.get());
  }

  @Test
  @DisplayName("Should handle order with special characters in email")
  void shouldHandleOrderWithSpecialCharactersInEmail() {
    testOrder.setCustomerEmail("test+special@example-domain.co.uk");

    when(invoiceRepository.findByOrderId(testOrder.getId())).thenReturn(Optional.empty());
    when(objectStoreService.uploadFile(anyString(), any(byte[].class), anyString())).thenReturn(testObjectUrl);
    when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

    Optional<String> result = invoiceService.generateInvoice(testOrder);

    assertTrue(result.isPresent());
    assertEquals(testObjectUrl, result.get());
  }
}