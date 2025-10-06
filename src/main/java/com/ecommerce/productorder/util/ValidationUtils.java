package com.ecommerce.productorder.util;

/**
 * Utility class for common validation operations
 * 
 * Design Principles Applied:
 * - Single Responsibility: Only handles validation logic
 * - Static Methods: Utility methods that don't require state
 * - Immutability: No mutable state
 * - Naming: Clear, descriptive method names
 * - Error Messages: Meaningful error messages
 */
public final class ValidationUtils {
    
    // Private constructor to prevent instantiation
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Validates that a string is not null or empty
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if value is null or empty
     */
    public static void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    
    /**
     * Validates that a number is positive
     * 
     * @param value the number to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if value is not positive
     */
    public static void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
    
    /**
     * Validates that a number is within a range
     * 
     * @param value the number to validate
     * @param min the minimum value (inclusive)
     * @param max the maximum value (inclusive)
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if value is not within range
     */
    public static void validateRange(Number value, double min, double max, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        
        double doubleValue = value.doubleValue();
        if (doubleValue < min || doubleValue > max) {
            throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max);
        }
    }
    
    /**
     * Validates that an object is not null
     * 
     * @param value the object to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if value is null
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }
    
    /**
     * Validates that a collection is not null or empty
     * 
     * @param collection the collection to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if collection is null or empty
     */
    public static void validateNotNullOrEmpty(java.util.Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }
    
    /**
     * Validates email format
     * 
     * @param email the email to validate
     * @throws IllegalArgumentException if email format is invalid
     */
    public static void validateEmail(String email) {
        validateNotNullOrEmpty(email, "Email");
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }
    
    /**
     * Validates that a string has minimum length
     * 
     * @param value the string to validate
     * @param minLength the minimum length required
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if string is too short
     */
    public static void validateMinLength(String value, int minLength, String fieldName) {
        validateNotNullOrEmpty(value, fieldName);
        
        if (value.length() < minLength) {
            throw new IllegalArgumentException(fieldName + " must be at least " + minLength + " characters long");
        }
    }
    
    /**
     * Validates that a string has maximum length
     * 
     * @param value the string to validate
     * @param maxLength the maximum length allowed
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if string is too long
     */
    public static void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }
}
