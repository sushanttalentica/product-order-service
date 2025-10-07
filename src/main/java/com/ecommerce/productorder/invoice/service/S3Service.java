package com.ecommerce.productorder.invoice.service;

import java.util.Optional;

/**
 * Service interface for AWS S3 operations
 * 
 * Design Principles Applied:
 * - Interface Segregation: Defines only necessary S3 operations
 * - Single Responsibility: Only handles S3 operations
 * - Dependency Inversion: Depends on abstractions, not implementations
 * - Optional Return Types: Uses Optional for null-safe operations
 * - Business Logic Encapsulation: Encapsulates S3 operations logic
 */
public interface S3Service {
    
    /**
     * Uploads file to S3
     * Uploads file content to S3 bucket
     * 
     * @param key the S3 key for the file
     * @param content the file content
     * @param contentType the content type of the file
     * @return S3 URL for the uploaded file
     * @throws IllegalArgumentException if parameters are invalid
     * @throws RuntimeException if upload fails
     */
    String uploadFile(String key, byte[] content, String contentType);
    
    /**
     * Downloads file from S3
     * Downloads file content from S3 bucket
     * 
     * @param key the S3 key for the file
     * @return Optional containing file content if found, empty otherwise
     */
    Optional<byte[]> downloadFile(String key);
    
    /**
     * Deletes file from S3
     * Removes file from S3 bucket
     * 
     * @param key the S3 key for the file
     * @return true if deletion successful, false otherwise
     */
    boolean deleteFile(String key);
    
    /**
     * Checks if file exists in S3
     * Verifies file existence in S3 bucket
     * 
     * @param key the S3 key for the file
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String key);
    
    /**
     * Gets file URL from S3
     * Retrieves public URL for file
     * 
     * @param key the S3 key for the file
     * @return Optional containing file URL if found, empty otherwise
     */
    Optional<String> getFileUrl(String key);
    
    /**
     * Generates presigned URL for file
     * Creates presigned URL for secure file access
     * 
     * @param key the S3 key for the file
     * @param expirationMinutes the expiration time in minutes
     * @return Optional containing presigned URL if successful, empty otherwise
     */
    Optional<String> generatePresignedUrl(String key, int expirationMinutes);
}
