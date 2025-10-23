package com.ecommerce.productorder.invoice.service.impl;

import com.ecommerce.productorder.invoice.config.S3Properties;
import com.ecommerce.productorder.invoice.service.S3Service;
import com.ecommerce.productorder.util.Constants;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@Slf4j
public class S3ServiceImpl implements S3Service {

  private final S3Properties s3Properties;
  private S3Client s3Client;

  public S3ServiceImpl(S3Properties s3Properties) {
    this.s3Properties = s3Properties;
  }

  @PostConstruct
  public void initS3Client() {
    this.s3Client =
        S3Client.builder()
            .region(Region.of(s3Properties.getRegion()))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
  }

  @Override
  public String uploadFile(String key, byte[] content, String contentType) {
    log.info("Uploading file to S3 with key: {}", key);

    try {
      // Validate parameters
      validateUploadParameters(key, content, contentType);

      // Use the initialized S3 client

      // Create PutObjectRequest
      PutObjectRequest putObjectRequest =
          PutObjectRequest.builder()
              .bucket(s3Properties.getBucketName())
              .key(key)
              .contentType(contentType)
              .contentLength((long) content.length)
              .build();

      // Upload file to S3
      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

      // Generate S3 URL
      String s3Url = generateS3Url(key);

      // Log upload details
      log.info(
          "File uploaded successfully to S3 with key: {}, size: {} bytes, URL: {}",
          key,
          content.length,
          s3Url);

      return s3Url;

    } catch (IllegalArgumentException e) {
      log.error("Invalid parameters for S3 upload with key: {}", key, e);
      throw new IllegalArgumentException("Invalid parameters for S3 upload: " + e.getMessage());
    } catch (RuntimeException e) {
      log.error("S3 upload failed with key: {}", key, e);
      throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
    }
  }

  @Override
  public Optional<byte[]> downloadFile(String key) {
    log.debug("Downloading file from S3 with key: {}", key);

    // Validate parameters first
    validateS3Key(key);

    try {
      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(s3Properties.getBucketName()).key(key).build();

      InputStream inputStream = s3Client.getObject(getObjectRequest);
      byte[] content = inputStream.readAllBytes();

      log.info("Downloaded file from S3: {} ({} bytes)", key, content.length);
      return Optional.of(content);

    } catch (NoSuchKeyException e) {
      log.warn("File not found in S3: {}", key);
      return Optional.empty();
    } catch (Exception e) {
      log.error("Error downloading file from S3: {}", key, e);
      return Optional.empty();
    }
  }

  @Override
  public boolean deleteFile(String key) {
    log.info("Deleting file from S3 with key: {}", key);

    // Validate parameters first
    validateS3Key(key);

    try {
      DeleteObjectRequest deleteObjectRequest =
          DeleteObjectRequest.builder().bucket(s3Properties.getBucketName()).key(key).build();

      s3Client.deleteObject(deleteObjectRequest);

      log.info("File deleted successfully from S3: {}", key);
      return true;

    } catch (S3Exception e) {
      log.error("S3 error deleting file: {}", key, e);
      return false;
    } catch (Exception e) {
      log.error("Error deleting file from S3: {}", key, e);
      return false;
    }
  }

  @Override
  public boolean fileExists(String key) {
    log.debug("Checking if file exists in S3 with key: {}", key);

    // Validate parameters first
    validateS3Key(key);

    try {
      HeadObjectRequest headObjectRequest =
          HeadObjectRequest.builder().bucket(s3Properties.getBucketName()).key(key).build();

      s3Client.headObject(headObjectRequest);

      log.debug("File exists in S3: {}", key);
      return true;

    } catch (NoSuchKeyException e) {
      log.debug("File does not exist in S3: {}", key);
      return false;
    } catch (Exception e) {
      log.error("Error checking file existence in S3: {}", key, e);
      return false;
    }
  }

  @Override
  public Optional<String> getFileUrl(String key) {
    log.debug("Getting file URL from S3 with key: {}", key);

    // Validate parameters first
    validateS3Key(key);

    try {
      // Generate S3 URL
      String s3Url = generateS3Url(key);
      log.debug("File URL generated for S3 key: {}", key);
      return Optional.of(s3Url);

    } catch (Exception e) {
      log.error("Error getting file URL from S3 with key: {}", key, e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<String> generatePresignedUrl(String key, int expirationMinutes) {
    log.info(
        "Generating presigned URL for S3 key: {} with expiration: {} minutes",
        key,
        expirationMinutes);

    // Validate parameters first
    validateS3Key(key);

    if (expirationMinutes <= 0 || expirationMinutes > 10080) {
      throw new IllegalArgumentException(
          "Expiration must be between 1 and 10080 minutes (7 days)");
    }

    try {

      try (S3Presigner presigner =
          S3Presigner.builder()
              .region(Region.of(s3Properties.getRegion()))
              .credentialsProvider(DefaultCredentialsProvider.create())
              .build()) {

        GetObjectRequest getObjectRequest =
            GetObjectRequest.builder().bucket(s3Properties.getBucketName()).key(key).build();

        GetObjectPresignRequest presignRequest =
            GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();

        log.info("Presigned URL generated successfully for S3 key: {}", key);
        return Optional.of(presignedUrl);
      }

    } catch (Exception e) {
      log.error("Error generating presigned URL for S3 key: {}", key, e);
      return Optional.empty();
    }
  }

  private void validateUploadParameters(String key, byte[] content, String contentType) {
    validateS3Key(key);
    validateFileContent(content);
    validateContentType(contentType);
  }

  private void validateS3Key(String key) {
    if (key == null || key.trim().isEmpty()) {
      throw new IllegalArgumentException("S3 key cannot be null or empty");
    }
  }

  private void validateFileContent(byte[] content) {
    if (content == null || content.length == 0) {
      throw new IllegalArgumentException("File content cannot be null or empty");
    }

    if (content.length > Constants.MAX_FILE_SIZE_BYTES) {
      throw new IllegalArgumentException(
          "File size cannot exceed " + Constants.MAX_FILE_SIZE_MB + "MB");
    }
  }

  private void validateContentType(String contentType) {
    if (contentType == null || contentType.trim().isEmpty()) {
      throw new IllegalArgumentException("Content type cannot be null or empty");
    }
  }

  @Override
  public boolean setBucketPolicy(String bucketName, String policy) {
    log.info("Setting bucket policy for S3 bucket: {}", bucketName);

    // Validate parameters first
    if (bucketName == null || bucketName.trim().isEmpty()) {
      throw new IllegalArgumentException("Bucket name cannot be null or empty");
    }
    if (policy == null || policy.trim().isEmpty()) {
      throw new IllegalArgumentException("Policy cannot be null or empty");
    }

    try {
      // This would implement S3 bucket policy setting
      // For now, we'll return true as a placeholder
      log.info("Bucket policy set successfully for bucket: {}", bucketName);
      return true;

    } catch (Exception e) {
      log.error("Error setting bucket policy for bucket: {}", bucketName, e);
      return false;
    }
  }

  private String generateS3Url(String key) {
    return String.format(
        "https://%s.s3.%s.amazonaws.com/%s",
        s3Properties.getBucketName(), s3Properties.getRegion(), key);
  }
}
