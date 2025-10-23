package com.ecommerce.productorder.invoice.service;

import java.util.Optional;

/**
 * Generic object storage service interface for storing and retrieving files.
 * This abstraction allows for different implementations (S3, Azure Blob, GCS, etc.)
 */
public interface ObjectStoreService {
  String uploadFile(String key, byte[] content, String contentType);
  Optional<byte[]> downloadFile(String key);
  boolean deleteFile(String key);
  boolean fileExists(String key);
  Optional<String> getFileUrl(String key);
  Optional<String> generatePresignedUrl(String key, int expirationMinutes);
  boolean setBucketPolicy(String bucketName, String policy);
}
