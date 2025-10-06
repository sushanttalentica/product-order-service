package com.ecommerce.productorder.exception;

/**
 * Custom exception for business rule violations
 * 
 * Design Principles Applied:
 * - Exception Hierarchy: Extends RuntimeException for unchecked exceptions
 * - Single Responsibility: Only handles business rule violations
 * - Encapsulation: Encapsulates error information
 * - Naming Convention: Clear and descriptive exception name
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
