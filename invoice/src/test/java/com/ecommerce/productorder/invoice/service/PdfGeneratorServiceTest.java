package com.ecommerce.productorder.invoice.service;

import static org.junit.jupiter.api.Assertions.*;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.OrderItem;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.invoice.service.impl.PdfGeneratorServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

// JUnit tests for PdfGeneratorService interface.
@ExtendWith(MockitoExtension.class)
@DisplayName("PdfGeneratorService Tests")
public class PdfGeneratorServiceTest {

  @InjectMocks private PdfGeneratorServiceImpl pdfGeneratorService;

  private Order testOrder;
  private OrderItem testOrderItem;
  private Product testProduct;

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
  }

  @Test
  @DisplayName("Should generate invoice PDF successfully")
  void shouldGenerateInvoicePdfSuccessfully() {
    byte[] result = pdfGeneratorService.generateInvoicePdf(testOrder);

    assertNotNull(result);
    assertTrue(result.length > 0);
  }

  @Test
  @DisplayName("Should generate receipt PDF successfully")
  void shouldGenerateReceiptPdfSuccessfully() {
    byte[] result = pdfGeneratorService.generateReceiptPdf(testOrder);

    assertNotNull(result);
    assertTrue(result.length > 0);
  }

  @Test
  @DisplayName("Should generate shipping label PDF successfully")
  void shouldGenerateShippingLabelPdfSuccessfully() {
    byte[] result = pdfGeneratorService.generateShippingLabelPdf(testOrder);

    assertNotNull(result);
    assertTrue(result.length > 0);
  }

  @Test
  @DisplayName("Should throw exception for null order in invoice PDF")
  void shouldThrowExceptionForNullOrderInInvoicePdf() {
    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateInvoicePdf(null));
  }

  @Test
  @DisplayName("Should throw exception for null order in receipt PDF")
  void shouldThrowExceptionForNullOrderInReceiptPdf() {
    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateReceiptPdf(null));
  }

  @Test
  @DisplayName("Should throw exception for null order in shipping label PDF")
  void shouldThrowExceptionForNullOrderInShippingLabelPdf() {
    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateShippingLabelPdf(null));
  }

  @Test
  @DisplayName("Should throw exception for order with zero total amount in invoice PDF")
  void shouldThrowExceptionForOrderWithZeroTotalAmountInInvoicePdf() {
    testOrder.setTotalAmount(BigDecimal.ZERO);

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateInvoicePdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with zero total amount in receipt PDF")
  void shouldThrowExceptionForOrderWithZeroTotalAmountInReceiptPdf() {
    testOrder.setTotalAmount(BigDecimal.ZERO);

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateReceiptPdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with zero total amount in shipping label PDF")
  void shouldThrowExceptionForOrderWithZeroTotalAmountInShippingLabelPdf() {
    testOrder.setTotalAmount(BigDecimal.ZERO);

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateShippingLabelPdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with null total amount in invoice PDF")
  void shouldThrowExceptionForOrderWithNullTotalAmountInInvoicePdf() {
    testOrder.setTotalAmount(null);

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateInvoicePdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with null total amount in receipt PDF")
  void shouldThrowExceptionForOrderWithNullTotalAmountInReceiptPdf() {
    testOrder.setTotalAmount(null);

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateReceiptPdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with null total amount in shipping label PDF")
  void shouldThrowExceptionForOrderWithNullTotalAmountInShippingLabelPdf() {
    testOrder.setTotalAmount(null);

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateShippingLabelPdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with null order items in invoice PDF")
  void shouldThrowExceptionForOrderWithNullOrderItemsInInvoicePdf() {
    testOrder.setOrderItems(null);

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateInvoicePdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with null order items in receipt PDF")
  void shouldThrowExceptionForOrderWithNullOrderItemsInReceiptPdf() {
    testOrder.setOrderItems(null);

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateReceiptPdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with null order items in shipping label PDF")
  void shouldThrowExceptionForOrderWithNullOrderItemsInShippingLabelPdf() {
    testOrder.setOrderItems(null);

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateShippingLabelPdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with empty order items in invoice PDF")
  void shouldThrowExceptionForOrderWithEmptyOrderItemsInInvoicePdf() {
    testOrder.setOrderItems(new ArrayList<>());

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateInvoicePdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with empty order items in receipt PDF")
  void shouldThrowExceptionForOrderWithEmptyOrderItemsInReceiptPdf() {
    testOrder.setOrderItems(new ArrayList<>());

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateReceiptPdf(testOrder));
  }

  @Test
  @DisplayName("Should throw exception for order with empty order items in shipping label PDF")
  void shouldThrowExceptionForOrderWithEmptyOrderItemsInShippingLabelPdf() {
    testOrder.setOrderItems(new ArrayList<>());

    assertThrows(IllegalArgumentException.class, () -> pdfGeneratorService.generateShippingLabelPdf(testOrder));
  }
}
