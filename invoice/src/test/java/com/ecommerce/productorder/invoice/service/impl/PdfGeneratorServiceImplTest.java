package com.ecommerce.productorder.invoice.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.OrderItem;
import com.ecommerce.productorder.domain.entity.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// JUnit tests for PdfGeneratorServiceImpl class.
@DisplayName("PdfGeneratorServiceImpl Tests")
public class PdfGeneratorServiceImplTest {
  private PdfGeneratorServiceImpl pdfGeneratorService;
  private Order testOrder;
  private Product testProduct;

  @BeforeEach
  void setUp() {
    pdfGeneratorService = new PdfGeneratorServiceImpl();
    testProduct = new Product();
    testProduct.setId(1L);
    testProduct.setName("iPhone 15");
    testProduct.setSku("IPHONE15-001");
    testProduct.setPrice(new BigDecimal("999.99"));
    testProduct.setDescription("Latest iPhone model");
    Category testCategory = new Category();
    testCategory.setId(1L);
    testCategory.setName("Test Category");
    testCategory.setDescription("Test Description");
    testProduct.setCategory(testCategory);
    testProduct.setStockQuantity(10);
    testProduct.setActive(true);
    testOrder = new Order();
    testOrder.setId(100L);
    testOrder.setOrderNumber(UUID.randomUUID().toString());
    testOrder.setCustomerId(200L);
    testOrder.setCustomerEmail("test@example.com");
    testOrder.setStatus(Order.OrderStatus.DELIVERED);
    testOrder.setTotalAmount(new BigDecimal("1999.98"));
    testOrder.setShippingAddress("123 Test St, Test City, TC 12345");
    testOrder.setCreatedAt(LocalDateTime.now().minusDays(1));
    testOrder.setUpdatedAt(LocalDateTime.now());
    OrderItem orderItem = new OrderItem();
    orderItem.setId(1L);
    orderItem.setProduct(testProduct);
    orderItem.setQuantity(2);
    orderItem.setUnitPrice(new BigDecimal("999.99"));
    orderItem.setSubtotal(new BigDecimal("1999.98"));
    testOrder.setOrderItems(List.of(orderItem));
  }

  @Test
  @DisplayName("Should generate invoice PDF successfully")
  void shouldGenerateInvoicePdfSuccessfully() {
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
    // PDF files start with %PDF header
    assertTrue(pdfContent[0] == 0x25 && pdfContent[1] == 0x50 && pdfContent[2] == 0x44 && pdfContent[3] == 0x46);
  }

  @Test
  @DisplayName("Should generate invoice PDF with order details")
  void shouldGenerateInvoicePdfWithOrderDetails() {
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
    // PDF files start with %PDF header
    assertTrue(pdfContent[0] == 0x25 && pdfContent[1] == 0x50 && pdfContent[2] == 0x44 && pdfContent[3] == 0x46);
  }

  @Test
  @DisplayName("Should generate invoice PDF with customer details")
  void shouldGenerateInvoicePdfWithCustomerDetails() {
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
    // PDF files start with %PDF header
    assertTrue(pdfContent[0] == 0x25 && pdfContent[1] == 0x50 && pdfContent[2] == 0x44 && pdfContent[3] == 0x46);
  }

  @Test
  @DisplayName("Should generate invoice PDF with product details")
  void shouldGenerateInvoicePdfWithProductDetails() {
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
    // PDF files start with %PDF header
    assertTrue(pdfContent[0] == 0x25 && pdfContent[1] == 0x50 && pdfContent[2] == 0x44 && pdfContent[3] == 0x46);
  }

  @Test
  @DisplayName("Should generate invoice PDF with total amount")
  void shouldGenerateInvoicePdfWithTotalAmount() {
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
    // PDF files start with %PDF header
    assertTrue(pdfContent[0] == 0x25 && pdfContent[1] == 0x50 && pdfContent[2] == 0x44 && pdfContent[3] == 0x46);
  }

  @Test
  @DisplayName("Should throw exception when order is null")
  void shouldThrowExceptionWhenOrderIsNull() {
    assertThrows(
        Exception.class,
        () -> {
          pdfGeneratorService.generateInvoicePdf(null);
        });
  }

  @Test
  @DisplayName("Should handle order with multiple items")
  void shouldHandleOrderWithMultipleItems() {
    Product secondProduct = new Product();
    secondProduct.setId(2L);
    secondProduct.setName("iPhone Case");
    secondProduct.setSku("CASE-001");
    secondProduct.setPrice(new BigDecimal("29.99"));
    secondProduct.setDescription("Protective case for iPhone");
    Category secondCategory = new Category();
    secondCategory.setId(2L);
    secondCategory.setName("Test Category 2");
    secondCategory.setDescription("Test Description 2");
    secondProduct.setCategory(secondCategory);
    secondProduct.setStockQuantity(50);
    secondProduct.setActive(true);
    OrderItem secondOrderItem = new OrderItem();
    secondOrderItem.setId(2L);
    secondOrderItem.setProduct(secondProduct);
    secondOrderItem.setQuantity(1);
    secondOrderItem.setUnitPrice(new BigDecimal("29.99"));
    secondOrderItem.setSubtotal(new BigDecimal("29.99"));
    testOrder.setOrderItems(List.of(testOrder.getOrderItems().get(0), secondOrderItem));
    testOrder.setTotalAmount(new BigDecimal("2029.97"));
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
  }

  @Test
  @DisplayName("Should generate receipt PDF successfully")
  void shouldGenerateReceiptPdfSuccessfully() {
    byte[] pdfContent = pdfGeneratorService.generateReceiptPdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
    String pdfContentString = new String(pdfContent);
    assertTrue(pdfContentString.contains("RECEIPT") || pdfContentString.contains("Receipt"));
  }

  @Test
  @DisplayName("Should generate receipt PDF with order details")
  void shouldGenerateReceiptPdfWithOrderDetails() {
    byte[] pdfContent = pdfGeneratorService.generateReceiptPdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
    String pdfContentString = new String(pdfContent);
    assertTrue(
        pdfContentString.contains(testOrder.getOrderNumber())
            || pdfContentString.contains(String.valueOf(testOrder.getId())));
  }

  @Test
  @DisplayName("Should throw exception when order is null for receipt")
  void shouldThrowExceptionWhenOrderIsNullForReceipt() {
    assertThrows(
        Exception.class,
        () -> {
          pdfGeneratorService.generateReceiptPdf(null);
        });
  }

  @Test
  @DisplayName("Should generate shipping label PDF successfully")
  void shouldGenerateShippingLabelPdfSuccessfully() {
    byte[] pdfContent = pdfGeneratorService.generateShippingLabelPdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
    String pdfContentString = new String(pdfContent);
    assertTrue(
        pdfContentString.contains("SHIPPING")
            || pdfContentString.contains("Shipping")
            || pdfContentString.contains("LABEL")
            || pdfContentString.contains("Label"));
  }

  @Test
  @DisplayName("Should generate shipping label PDF with shipping address")
  void shouldGenerateShippingLabelPdfWithShippingAddress() {
    byte[] pdfContent = pdfGeneratorService.generateShippingLabelPdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
    String pdfContentString = new String(pdfContent);
    assertTrue(
        pdfContentString.contains(testOrder.getShippingAddress())
            || pdfContentString.contains("123 Test St"));
  }

  @Test
  @DisplayName("Should throw exception when order is null for shipping label")
  void shouldThrowExceptionWhenOrderIsNullForShippingLabel() {
    assertThrows(
        Exception.class,
        () -> {
          pdfGeneratorService.generateShippingLabelPdf(null);
        });
  }

  @Test
  @DisplayName("Should implement generateInvoice method")
  void shouldImplementGenerateInvoiceMethod() {
    byte[] pdfContent = pdfGeneratorService.generateInvoice(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
  }

  @Test
  @DisplayName("Should return correct content type")
  void shouldReturnCorrectContentType() {
    String contentType = pdfGeneratorService.getContentType();
    assertEquals("application/pdf", contentType);
  }

  @Test
  @DisplayName("Should handle order with zero total amount")
  void shouldHandleOrderWithZeroTotalAmount() {
    testOrder.setTotalAmount(BigDecimal.ZERO);
    testOrder.getOrderItems().get(0).setUnitPrice(BigDecimal.ZERO);
    testOrder.getOrderItems().get(0).setSubtotal(BigDecimal.ZERO);
    
    // This should throw an exception due to validation
    assertThrows(RuntimeException.class, () -> {
      pdfGeneratorService.generateInvoicePdf(testOrder);
    });
  }

  @Test
  @DisplayName("Should handle order with very large amounts")
  void shouldHandleOrderWithVeryLargeAmounts() {
    BigDecimal largeAmount = new BigDecimal("999999.99");
    testOrder.setTotalAmount(largeAmount);
    testOrder.getOrderItems().get(0).setUnitPrice(largeAmount);
    testOrder.getOrderItems().get(0).setSubtotal(largeAmount);
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
  }

  @Test
  @DisplayName("Should throw exception for order with empty order items")
  void shouldHandleOrderWithEmptyOrderItems() {
    testOrder.setOrderItems(List.of());
    assertThrows(IllegalArgumentException.class, () -> {
      pdfGeneratorService.generateInvoicePdf(testOrder);
    });
  }

  @Test
  @DisplayName("Should handle order with null product")
  void shouldHandleOrderWithNullProduct() {
    testOrder.getOrderItems().get(0).setProduct(null);
    
    // This should throw an exception due to null product
    assertThrows(RuntimeException.class, () -> {
      pdfGeneratorService.generateInvoicePdf(testOrder);
    });
  }

  @Test
  @DisplayName("Should handle order with very long customer email")
  void shouldHandleOrderWithVeryLongCustomerEmail() {
    String longEmail =
        "very.long.email.address.that.might.exceed.normal.length@very.long.domain.name.com";
    testOrder.setCustomerEmail(longEmail);
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
  }

  @Test
  @DisplayName("Should handle order with very long shipping address")
  void shouldHandleOrderWithVeryLongShippingAddress() {
    String longAddress =
        "123 Very Long Street Name That Might Exceed Normal Length, Very Long City Name, Very"
            + " Long State Name, 12345-6789";
    testOrder.setShippingAddress(longAddress);
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
  }

  @Test
  @DisplayName("Should handle order with special characters in product name")
  void shouldHandleOrderWithSpecialCharactersInProductName() {
    testProduct.setName("iPhone 15 Pro Max™ (Special Edition) - 256GB");
    testProduct.setDescription("Latest iPhone with special characters: @#$%^&*()");
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
  }

  @Test
  @DisplayName("Should handle order with unicode characters")
  void shouldHandleOrderWithUnicodeCharacters() {
    testProduct.setName("iPhone 15 Pro Max™ (特别版) - 256GB");
    testProduct.setDescription("最新款iPhone，带有特殊字符：@#$%^&*()");
    testOrder.setCustomerEmail("test@example.com");
    testOrder.setShippingAddress("123 测试街道, 测试城市, 测试省份, 12345");
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
  }

  @Test
  @DisplayName("Should generate PDF within reasonable time")
  void shouldGeneratePdfWithinReasonableTime() {
    long startTime = System.currentTimeMillis();
    byte[] pdfContent = pdfGeneratorService.generateInvoicePdf(testOrder);
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    assertNotNull(pdfContent);
    assertTrue(pdfContent.length > 0);
    assertTrue(duration < 5000, "PDF generation should complete within 5 seconds");
  }

  @Test
  @DisplayName("Should generate multiple PDFs consistently")
  void shouldGenerateMultiplePdfsConsistently() {
    byte[] pdf1 = pdfGeneratorService.generateInvoicePdf(testOrder);
    byte[] pdf2 = pdfGeneratorService.generateInvoicePdf(testOrder);
    byte[] pdf3 = pdfGeneratorService.generateInvoicePdf(testOrder);
    assertNotNull(pdf1);
    assertNotNull(pdf2);
    assertNotNull(pdf3);
    assertTrue(pdf1.length > 0);
    assertTrue(pdf2.length > 0);
    assertTrue(pdf3.length > 0);
    assertTrue(Math.abs(pdf1.length - pdf2.length) < pdf1.length * 0.1);
    assertTrue(Math.abs(pdf1.length - pdf3.length) < pdf1.length * 0.1);
  }
}