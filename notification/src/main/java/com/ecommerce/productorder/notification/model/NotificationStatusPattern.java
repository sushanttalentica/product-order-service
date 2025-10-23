package com.ecommerce.productorder.notification.model;


// Enum for notification status patterns to avoid magic strings.
public enum NotificationStatusPattern {
  ORDER_CONFIRMATION("Order Confirmation - %s"),
  ORDER_STATUS_UPDATE("Order Status Update - %s"),
  ORDER_CANCELLATION("Order Cancelled - %s"),
  PAYMENT_CONFIRMATION("Payment Confirmed - %s"),
  INVOICE("Invoice - %s"),
  LOW_STOCK_ALERT("Low Stock Alert - %s");

  private final String pattern;

  NotificationStatusPattern(String pattern) {
    this.pattern = pattern;
  }

  // Format the pattern with the provided arguments

  public String format(Object... args) {
    return String.format(pattern, args);
  }

  // Get and the pattern string
  public String getPattern() {
    return pattern;
  }
}
