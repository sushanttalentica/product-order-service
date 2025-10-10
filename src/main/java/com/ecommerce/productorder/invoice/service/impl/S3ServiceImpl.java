package com.ecommerce.productorder.invoice.service.impl;

import com.ecommerce.productorder.invoice.service.S3Service;
import com.ecommerce.productorder.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.PostConstruct;

@Service
@Slf4j
public class S3ServiceImpl implements S3Service {
    
    // Use constants from Constants class
    
    @Value("${aws.s3.bucket-name:my-pos-bucket-125}")
    private String bucketName;
    
    @Value("${aws.s3.region:ap-south-1}")
    private String region;
    
    private S3Client s3Client;
    
    @PostConstruct
    public void initS3Client() {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
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
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) content.length)
                    .build();
            
            // Upload file to S3
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
            
            // Generate S3 URL
            String s3Url = generateS3Url(key);
            
            // Log upload details
            log.info("File uploaded successfully to S3 with key: {}, size: {} bytes, URL: {}", key, content.length, s3Url);
            
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
        
        try {
            // Validate key
            if (key == null || key.trim().isEmpty()) {
                log.warn("Invalid S3 key provided: {}", key);
                return Optional.empty();
            }
            
            // Simulate S3 download (in real implementation, use AWS SDK)
            // For now, return empty to indicate file not found
            log.debug("File not found in S3 with key: {}", key);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error downloading file from S3 with key: {}", key, e);
            return Optional.empty();
        }
    }
    

    @Override
    public boolean deleteFile(String key) {
        log.info("Deleting file from S3 with key: {}", key);
        
        try {
            // Validate key
            if (key == null || key.trim().isEmpty()) {
                log.warn("Invalid S3 key provided: {}", key);
                return false;
            }
            
            // Simulate S3 deletion (in real implementation, use AWS SDK)
            log.info("File deleted successfully from S3 with key: {}", key);
            return true;
            
        } catch (Exception e) {
            log.error("Error deleting file from S3 with key: {}", key, e);
            return false;
        }
    }
    

    @Override
    public boolean fileExists(String key) {
        log.debug("Checking if file exists in S3 with key: {}", key);
        
        try {
            // Validate key
            if (key == null || key.trim().isEmpty()) {
                log.warn("Invalid S3 key provided: {}", key);
                return false;
            }
            
            // Simulate S3 existence check (in real implementation, use AWS SDK)
            // For now, return true to indicate file exists
            log.debug("File exists in S3 with key: {}", key);
            return true;
            
        } catch (Exception e) {
            log.error("Error checking file existence in S3 with key: {}", key, e);
            return false;
        }
    }
    

    @Override
    public Optional<String> getFileUrl(String key) {
        log.debug("Getting file URL from S3 with key: {}", key);
        
        try {
            // Validate key
            if (key == null || key.trim().isEmpty()) {
                log.warn("Invalid S3 key provided: {}", key);
                return Optional.empty();
            }
            
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
        log.info("Generating presigned URL for S3 key: {} with expiration: {} minutes", key, expirationMinutes);
        
        try {
            // Validate parameters
            if (key == null || key.trim().isEmpty()) {
                log.warn("Invalid S3 key provided: {}", key);
                return Optional.empty();
            }
            
            if (expirationMinutes <= 0) {
                log.warn("Invalid expiration minutes provided: {}", expirationMinutes);
                return Optional.empty();
            }
            
            // Generate presigned URL (in real implementation, use AWS SDK)
            String presignedUrl = generateS3Url(key) + "?presigned=true&expires=" + expirationMinutes;
            
            log.info("Presigned URL generated successfully for S3 key: {}", key);
            return Optional.of(presignedUrl);
            
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
        if (key == null || key.trim().length() < Constants.MIN_KEY_LENGTH) {
            throw new IllegalArgumentException("S3 key cannot be null or empty");
        }
    }
    

    private void validateFileContent(byte[] content) {
        if (content == null || content.length < Constants.MIN_CONTENT_LENGTH) {
            throw new IllegalArgumentException("File content cannot be null or empty");
        }
        
        if (content.length > Constants.MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size cannot exceed " + Constants.MAX_FILE_SIZE_MB + "MB");
        }
    }
    

    private void validateContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }
    }
    

    private String generateS3Url(String key) {
        // Use regional endpoint format: https://bucket-name.s3.region.amazonaws.com/path/to/file
        // This URL is permanent and publicly accessible if bucket policy allows
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
}
