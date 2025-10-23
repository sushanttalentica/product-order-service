package com.ecommerce.productorder.invoice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.OrderItem;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.invoice.domain.entity.Invoice;
import com.ecommerce.productorder.invoice.domain.repository.InvoiceRepository;
import com.ecommerce.productorder.invoice.service.impl.InvoiceServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// JUnit tests for InvoiceService interface.
@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceService Tests")
public class InvoiceServiceTest {

  @Mock private InvoiceRepository invoiceRepository;
  @Mock private InvoiceGeneratorService invoiceGeneratorService;
  @Mock private ObjectStoreService objectStoreService;

  @InjectMocks private InvoiceServiceImpl invoiceService;

  private Order testOrder;
  private OrderItem testOrderItem;
  private Product testProduct;
  private Invoice testInvoice;

  @BeforeEach
  void setUp() {
    testProduct = new Product();
    testProduct.setId(1L);
    testProduct.setName("Test Product");
    testProduct.setPrice(new BigDecimal("99.99"));

    testOrderItem = new OrderItem();
    testOrderItem.setId(1L);
    testOrderItem.setProduct(testProduct);
    testOrderItem.setQuantity(2);
    testOrderItem.setUnitPrice(new BigDecimal("99.99"));

    testOrder = new Order();
    testOrder.setId(100L);
    testOrder.setCustomerId(200L);
    testOrder.setCustomerEmail("test@example.com");
    testOrder.setTotalAmount(new BigDecimal("199.98"));
    testOrder.setStatus(Order.OrderStatus.DELIVERED);
    testOrder.setCreatedAt(LocalDateTime.now());

    List<OrderItem> orderItems = new ArrayList<>();
    orderItems.add(testOrderItem);
    testOrder.setOrderItems(orderItems);

    testInvoice = new Invoice();
    testInvoice.setId(1L);
    testInvoice.setOrderId(100L);
    testInvoice.setCustomerId(200L);
    testInvoice.setCustomerEmail("test@example.com");
    testInvoice.setTotalAmount(new BigDecimal("199.98"));
    testInvoice.setObjectKey("invoices/100/test-invoice.pdf");
    testInvoice.setObjectUrl("https://example.com/invoices/100/test-invoice.pdf");
    testInvoice.setStatus(Invoice.InvoiceStatus.GENERATED);
    testInvoice.setGeneratedAt(LocalDateTime.now());
    testInvoice.setCreatedAt(LocalDateTime.now());
    testInvoice.setUpdatedAt(LocalDateTime.now());
  }

  @Test
  @DisplayName("Should generate invoice successfully")
  void shouldGenerateInvoiceSuccessfully() {
    when(invoiceRepository.findByOrderId(100L)).thenReturn(Optional.empty());
    when(invoiceGeneratorService.generateInvoice(testOrder)).thenReturn(new byte[]{1, 2, 3});
    when(invoiceGeneratorService.getContentType()).thenReturn("application/pdf");
    when(objectStoreService.uploadFile(anyString(), any(byte[].class), anyString()))
        .thenReturn("https://example.com/invoices/100/test-invoice.pdf");
    when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);

    Optional<String> result = invoiceService.generateInvoice(testOrder);

    assertTrue(result.isPresent());
    assertEquals("https://example.com/invoices/100/test-invoice.pdf", result.get());
    verify(invoiceRepository).save(any(Invoice.class));
  }

  @Test
  @DisplayName("Should return existing invoice URL if invoice already exists")
  void shouldReturnExistingInvoiceUrlIfInvoiceAlreadyExists() {
    when(invoiceRepository.findByOrderId(100L)).thenReturn(Optional.of(testInvoice));

    Optional<String> result = invoiceService.generateInvoice(testOrder);

    assertTrue(result.isPresent());
    assertEquals("https://example.com/invoices/100/test-invoice.pdf", result.get());
    verify(invoiceRepository, never()).save(any(Invoice.class));
  }

  @Test
  @DisplayName("Should get invoice URL successfully")
  void shouldGetInvoiceUrlSuccessfully() {
    when(invoiceRepository.findByOrderId(100L)).thenReturn(Optional.of(testInvoice));

    Optional<String> result = invoiceService.getInvoiceUrl(100L);

    assertTrue(result.isPresent());
    assertEquals("https://example.com/invoices/100/test-invoice.pdf", result.get());
  }

  @Test
  @DisplayName("Should return empty when invoice not found for URL")
  void shouldReturnEmptyWhenInvoiceNotFoundForUrl() {
    when(invoiceRepository.findByOrderId(100L)).thenReturn(Optional.empty());

    Optional<String> result = invoiceService.getInvoiceUrl(100L);

    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Should delete invoice successfully")
  void shouldDeleteInvoiceSuccessfully() {
    when(invoiceRepository.findByOrderId(100L)).thenReturn(Optional.of(testInvoice));
    when(objectStoreService.deleteFile("invoices/100/test-invoice.pdf")).thenReturn(true);
    doNothing().when(invoiceRepository).delete(testInvoice);

    boolean result = invoiceService.deleteInvoice(100L);

    assertTrue(result);
    verify(invoiceRepository).delete(testInvoice);
    verify(objectStoreService).deleteFile("invoices/100/test-invoice.pdf");
  }

  @Test
  @DisplayName("Should return false when invoice not found for deletion")
  void shouldReturnFalseWhenInvoiceNotFoundForDeletion() {
    when(invoiceRepository.findByOrderId(100L)).thenReturn(Optional.empty());

    boolean result = invoiceService.deleteInvoice(100L);

    assertFalse(result);
    verify(invoiceRepository, never()).delete(any(Invoice.class));
  }

  @Test
  @DisplayName("Should check if invoice exists")
  void shouldCheckIfInvoiceExists() {
    when(invoiceRepository.findByOrderId(100L)).thenReturn(Optional.of(testInvoice));

    boolean result = invoiceService.invoiceExists(100L);

    assertTrue(result);
  }

  @Test
  @DisplayName("Should return false when invoice does not exist")
  void shouldReturnFalseWhenInvoiceDoesNotExist() {
    when(invoiceRepository.findByOrderId(100L)).thenReturn(Optional.empty());

    boolean result = invoiceService.invoiceExists(100L);

    assertFalse(result);
  }

  @Test
  @DisplayName("Should throw exception for null order ID in get URL")
  void shouldThrowExceptionForNullOrderIdInGetUrl() {
    assertThrows(IllegalArgumentException.class, () -> invoiceService.getInvoiceUrl(null));
  }

  @Test
  @DisplayName("Should throw exception for null order ID in delete")
  void shouldThrowExceptionForNullOrderIdInDelete() {
    assertThrows(IllegalArgumentException.class, () -> invoiceService.deleteInvoice(null));
  }

  @Test
  @DisplayName("Should throw exception for null order ID in exists check")
  void shouldThrowExceptionForNullOrderIdInExistsCheck() {
    assertThrows(IllegalArgumentException.class, () -> invoiceService.invoiceExists(null));
  }
}
