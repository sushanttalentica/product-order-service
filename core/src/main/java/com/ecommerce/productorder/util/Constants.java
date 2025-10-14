package com.ecommerce.productorder.util;

public final class Constants {
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    public static final int HOURS_FOR_ATTENTION_CHECK = 24;
    public static final int SCALE_FOR_DECIMAL_OPERATIONS = 2;
    public static final int MIN_ORDER_ITEMS = 1;
    public static final int MAX_ORDER_ITEMS = 100;
    
    public static final int MIN_KEY_LENGTH = 1;
    public static final int MIN_CONTENT_LENGTH = 1;
    public static final int MIN_EXPIRATION_MINUTES = 1;
    public static final int MAX_EXPIRATION_MINUTES = 1440; // 24 hours
    public static final int MAX_FILE_SIZE_MB = 100;
    public static final int MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    
    // Pagination Constants
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    // Cache Constants
    public static final String PRODUCT_CACHE_PREFIX = "product:";
    public static final String CUSTOMER_CACHE_PREFIX = "customer:";
    public static final String ORDER_CACHE_PREFIX = "order:";
    
    // Event Kafka Topics
    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String ORDER_STATUS_UPDATED_TOPIC = "order.status.updated";
    public static final String ORDER_CANCELLED_TOPIC = "order.cancelled";
    public static final String ORDER_COMPLETED_TOPIC = "order.completed";
    public static final String PAYMENT_PROCESSED_TOPIC = "payment.processed";
    public static final String PAYMENT_REFUNDED_TOPIC = "payment.refunded";
    
    // HTTP Status Messages
    public static final String SUCCESS_MESSAGE = "Operation completed successfully";
    public static final String ERROR_MESSAGE = "An error occurred while processing the request";
    public static final String VALIDATION_ERROR_MESSAGE = "Validation failed for the provided data";
    
    // Date Formats
    public static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_TIME_FORMAT = "HH:mm:ss";
    
    // Security Constants
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    
    // Database Constants
    public static final String DEFAULT_SCHEMA = "public";
    public static final int DEFAULT_BATCH_SIZE = 25;
    
    // S3 Constants
    public static final String S3_INVOICE_PREFIX = "invoices/";
    public static final String PDF_CONTENT_TYPE = "application/pdf";
    public static final String DEFAULT_S3_REGION = "ap-south-1";
}
