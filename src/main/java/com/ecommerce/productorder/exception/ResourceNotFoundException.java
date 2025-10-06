package com.ecommerce.productorder.exception;

/**
 * Custom exception for resource not found scenarios
 * 
 * Design Principles Applied:
 * - Exception Hierarchy: Extends RuntimeException for unchecked exceptions
 * - Single Responsibility: Only handles resource not found scenarios
 * - Encapsulation: Encapsulates error information
 * - Naming Convention: Clear and descriptive exception name
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
