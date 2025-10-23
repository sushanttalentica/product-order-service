package com.ecommerce.productorder.invoice.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.OrderItem;
import com.ecommerce.productorder.invoice.service.PdfGeneratorService;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PdfGeneratorServiceImpl implements PdfGeneratorService {

  public PdfGeneratorServiceImpl() {}

  @Override
  public byte[] generateInvoice(Order order) {
    return generateInvoicePdf(order);
  }

  @Override
  public String getContentType() {
    return "application/pdf";
  }

  @Override
  public byte[] generateInvoicePdf(Order order) {
    // Validate order first
    validateOrderForPdf(order);
    
    log.info("Generating PDF invoice for order ID: {}", order.getId());

    try {

      // Create PDF using iText
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PdfWriter writer = new PdfWriter(outputStream);
      PdfDocument pdfDoc = new PdfDocument(writer);
      Document document = new Document(pdfDoc);

      // Add invoice header
      Paragraph header =
          new Paragraph("INVOICE")
              .setFontSize(24)
              .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
              .setTextAlignment(TextAlignment.CENTER);
      document.add(header);

      document.add(new Paragraph("\n"));

      // Add invoice details
      document.add(new Paragraph("Invoice Number: " + order.getOrderNumber()));
      document.add(
          new Paragraph(
              "Date: "
                  + LocalDateTime.now()
                      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
      document.add(new Paragraph("Customer Email: " + order.getCustomerEmail()));
      document.add(new Paragraph("Shipping Address: " + order.getShippingAddress()));

      document.add(new Paragraph("\n"));

      // Add order items table
      Paragraph itemsHeader =
          new Paragraph("Order Items:")
              .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
      document.add(itemsHeader);
      Table table = new Table(4);
      table.addHeaderCell("Product");
      table.addHeaderCell("Quantity");
      table.addHeaderCell("Unit Price");
      table.addHeaderCell("Subtotal");

      for (OrderItem item : order.getOrderItems()) {
        table.addCell(item.getProduct().getName());
        table.addCell(String.valueOf(item.getQuantity()));
        table.addCell("$" + item.getUnitPrice());
        table.addCell("$" + item.getSubtotal());
      }

      document.add(table);

      document.add(new Paragraph("\n"));

      // Add total
      Paragraph total =
          new Paragraph("Total Amount: $" + order.getTotalAmount())
              .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
              .setFontSize(14);
      document.add(total);

      document.add(new Paragraph("\nStatus: " + order.getStatus()));
      document.add(
          new Paragraph(
              "Order Date: "
                  + order
                      .getCreatedAt()
                      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

      // Close document
      document.close();

      byte[] pdfBytes = outputStream.toByteArray();
      log.info(
          "PDF invoice generated successfully for order ID: {} ({} bytes)",
          order.getId(),
          pdfBytes.length);
      return pdfBytes;

    } catch (Exception e) {
      log.error("Error generating PDF invoice for order ID: {}", order.getId(), e);
      throw new RuntimeException("Failed to generate PDF invoice: " + e.getMessage());
    }
  }

  @Override
  public byte[] generateReceiptPdf(Order order) {
    // Validate order first
    validateOrderForPdf(order);
    
    log.info("Generating PDF receipt for order ID: {}", order.getId());

    try {

      // Generate PDF content
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      // Create PDF content (simplified implementation)
      String pdfContent = createReceiptContent(order);
      outputStream.write(pdfContent.getBytes());
      outputStream.close();

      byte[] pdfBytes = outputStream.toByteArray();

      log.info("PDF receipt generated successfully for order ID: {}", order.getId());
      return pdfBytes;

    } catch (IOException e) {
      log.error("Error generating PDF receipt for order ID: {}", order.getId(), e);
      throw new RuntimeException("Failed to generate PDF receipt: " + e.getMessage());
    } catch (Exception e) {
      log.error("Error generating PDF receipt for order ID: {}", order.getId(), e);
      throw new RuntimeException("Failed to generate PDF receipt: " + e.getMessage());
    }
  }

  @Override
  public byte[] generateShippingLabelPdf(Order order) {
    // Validate order first
    validateOrderForPdf(order);
    
    log.info("Generating PDF shipping label for order ID: {}", order.getId());

    try {

      // Generate PDF content
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      // Create PDF content (simplified implementation)
      String pdfContent = createShippingLabelContent(order);
      outputStream.write(pdfContent.getBytes());
      outputStream.close();

      byte[] pdfBytes = outputStream.toByteArray();

      log.info("PDF shipping label generated successfully for order ID: {}", order.getId());
      return pdfBytes;

    } catch (IOException e) {
      log.error("Error generating PDF shipping label for order ID: {}", order.getId(), e);
      throw new RuntimeException("Failed to generate PDF shipping label: " + e.getMessage());
    } catch (Exception e) {
      log.error("Error generating PDF shipping label for order ID: {}", order.getId(), e);
      throw new RuntimeException("Failed to generate PDF shipping label: " + e.getMessage());
    }
  }

  private void validateOrderForPdf(Order order) {
    if (order == null) {
      throw new IllegalArgumentException("Order cannot be null");
    }

    if (order.getId() == null) {
      throw new IllegalArgumentException("Order ID cannot be null");
    }

    if (order.getCustomerEmail() == null || order.getCustomerEmail().trim().isEmpty()) {
      throw new IllegalArgumentException("Customer email is required");
    }

    if (order.getTotalAmount() == null
        || order.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Order total amount must be greater than zero");
    }

    if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
      throw new IllegalArgumentException("Order items cannot be null or empty");
    }
  }

  private String createReceiptContent(Order order) {
    StringBuilder content = new StringBuilder();

    // Receipt header
    content.append("RECEIPT\n");
    content.append("=======\n\n");

    // Receipt details
    content.append("Receipt Number: ").append(order.getOrderNumber()).append("\n");
    content
        .append("Date: ")
        .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        .append("\n");
    content.append("Customer ID: ").append(order.getCustomerId()).append("\n");
    content.append("Customer Email: ").append(order.getCustomerEmail()).append("\n");
    content.append("Order Status: ").append(order.getStatus()).append("\n");
    content.append("Total Amount: $").append(order.getTotalAmount()).append("\n");

    content.append("\n");
    content.append("Payment received. Thank you!\n");

    return content.toString();
  }

  private String createShippingLabelContent(Order order) {
    StringBuilder content = new StringBuilder();

    // Shipping label header
    content.append("SHIPPING LABEL\n");
    content.append("==============\n\n");

    // Shipping details
    content.append("Order Number: ").append(order.getOrderNumber()).append("\n");
    content.append("Customer ID: ").append(order.getCustomerId()).append("\n");
    content.append("Customer Email: ").append(order.getCustomerEmail()).append("\n");

    if (order.getShippingAddress() != null) {
      content.append("Shipping Address: ").append(order.getShippingAddress()).append("\n");
    }

    content.append("\n");
    content.append("Handle with care!\n");

    return content.toString();
  }
}
