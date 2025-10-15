package com.ecommerce.productorder.invoice.service;

import java.util.Optional;

public interface S3Service {

  String uploadFile(String key, byte[] content, String contentType);

  Optional<byte[]> downloadFile(String key);

  boolean deleteFile(String key);

  boolean fileExists(String key);

  Optional<String> getFileUrl(String key);

  Optional<String> generatePresignedUrl(String key, int expirationMinutes);
}
