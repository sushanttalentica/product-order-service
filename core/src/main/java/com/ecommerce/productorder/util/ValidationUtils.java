package com.ecommerce.productorder.util;

import java.util.Collection;

public final class ValidationUtils {
    
    // Private constructor to prevent instantiation
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    

    public static void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    

    public static void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
    

    public static void validateRange(Number value, double min, double max, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        
        double doubleValue = value.doubleValue();
        if (doubleValue < min || doubleValue > max) {
            throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max);
        }
    }
    

    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }
    

    public static void validateNotNullOrEmpty(Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    

    public static void validateEmail(String email) {
        validateNotNullOrEmpty(email, "Email");
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }
    

    public static void validateMinLength(String value, int minLength, String fieldName) {
        validateNotNullOrEmpty(value, fieldName);
        
        if (value.length() < minLength) {
            throw new IllegalArgumentException(fieldName + " must be at least " + minLength + " characters long");
        }
    }
    

    public static void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }
}
