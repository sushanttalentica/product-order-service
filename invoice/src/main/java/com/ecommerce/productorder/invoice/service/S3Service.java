package com.ecommerce.productorder.invoice.service;

import java.util.Optional;

// This interface provides S3-specific functionality while maintaining the generic
// ObjectStoreService contract.
public interface S3Service extends ObjectStoreService {

  Optional<String> generatePresignedUrl(String key, int expirationMinutes);

  boolean setBucketPolicy(String bucketName, String policy);
}
