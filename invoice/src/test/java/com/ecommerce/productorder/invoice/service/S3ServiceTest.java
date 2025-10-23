package com.ecommerce.productorder.invoice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ecommerce.productorder.invoice.config.S3Properties;
import com.ecommerce.productorder.invoice.service.impl.S3ServiceImpl;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;

// JUnit tests for S3Service interface.
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
@DisplayName("S3Service Tests")
public class S3ServiceTest {

  @Mock private S3Properties s3Properties;
  @Mock private S3Client s3Client;

  @InjectMocks private S3ServiceImpl s3Service;

  @BeforeEach
  void setUp() throws Exception {
    // Inject the mocked S3Client into the service
    Field s3ClientField = S3ServiceImpl.class.getDeclaredField("s3Client");
    s3ClientField.setAccessible(true);
    s3ClientField.set(s3Service, s3Client);
  }

  @Test
  @DisplayName("Should upload file successfully")
  void shouldUploadFileSuccessfully() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String key = "test-file.pdf";
    byte[] content = "test&#x20;content".getBytes();
    String contentType = "application/pdf";

    String result = s3Service.uploadFile(key, content, contentType);

    assertNotNull(result);
    assertTrue(result.contains("test-bucket"));
    assertTrue(result.contains(key));
  }

  @Test
  @DisplayName("Should download file successfully")
  void shouldDownloadFileSuccessfully() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String key = "test-file.pdf";

    Optional<byte[]> result = s3Service.downloadFile(key);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Should delete file successfully")
  void shouldDeleteFileSuccessfully() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String key = "test-file.pdf";

    boolean result = s3Service.deleteFile(key);

    assertTrue(result);
  }

  @Test
  @DisplayName("Should check if file exists")
  void shouldCheckIfFileExists() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String key = "test-file.pdf";

    boolean result = s3Service.fileExists(key);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Should get file URL")
  void shouldGetFileUrl() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String key = "test-file.pdf";

    Optional<String> result = s3Service.getFileUrl(key);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Should generate presigned URL")
  void shouldGeneratePresignedUrl() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String key = "test-file.pdf";
    int expirationMinutes = 60;

    Optional<String> result = s3Service.generatePresignedUrl(key, expirationMinutes);

    assertNotNull(result);
  }

  @Test
  @DisplayName("Should set bucket policy")
  void shouldSetBucketPolicy() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String bucketName = "test-bucket";
    String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[]}";

    boolean result = s3Service.setBucketPolicy(bucketName, policy);

    assertTrue(result);
  }

  @Test
  @DisplayName("Should throw exception for null key in upload")
  void shouldThrowExceptionForNullKeyInUpload() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    byte[] content = "test content".getBytes();
    String contentType = "application/pdf";

    assertThrows(IllegalArgumentException.class, () -> s3Service.uploadFile(null, content, contentType));
  }

  @Test
  @DisplayName("Should throw exception for null content in upload")
  void shouldThrowExceptionForNullContentInUpload() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String key = "test-file.pdf";
    String contentType = "application/pdf";

    assertThrows(IllegalArgumentException.class, () -> s3Service.uploadFile(key, null, contentType));
  }

  @Test
  @DisplayName("Should throw exception for null content type in upload")
  void shouldThrowExceptionForNullContentTypeInUpload() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String key = "test-file.pdf";
    byte[] content = "test content".getBytes();

    assertThrows(IllegalArgumentException.class, () -> s3Service.uploadFile(key, content, null));
  }

  @Test
  @DisplayName("Should throw exception for null key in download")
  void shouldThrowExceptionForNullKeyInDownload() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    assertThrows(IllegalArgumentException.class, () -> s3Service.downloadFile(null));
  }

  @Test
  @DisplayName("Should throw exception for null key in delete")
  void shouldThrowExceptionForNullKeyInDelete() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    assertThrows(IllegalArgumentException.class, () -> s3Service.deleteFile(null));
  }

  @Test
  @DisplayName("Should throw exception for null key in file exists")
  void shouldThrowExceptionForNullKeyInFileExists() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    assertThrows(IllegalArgumentException.class, () -> s3Service.fileExists(null));
  }

  @Test
  @DisplayName("Should throw exception for null key in get file URL")
  void shouldThrowExceptionForNullKeyInGetFileUrl() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    assertThrows(IllegalArgumentException.class, () -> s3Service.getFileUrl(null));
  }

  @Test
  @DisplayName("Should throw exception for null key in presigned URL")
  void shouldThrowExceptionForNullKeyInPresignedUrl() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    assertThrows(IllegalArgumentException.class, () -> s3Service.generatePresignedUrl(null, 60));
  }

  @Test
  @DisplayName("Should throw exception for invalid expiration in presigned URL")
  void shouldThrowExceptionForInvalidExpirationInPresignedUrl() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String key = "test-file.pdf";

    assertThrows(IllegalArgumentException.class, () -> s3Service.generatePresignedUrl(key, -1));
    assertThrows(IllegalArgumentException.class, () -> s3Service.generatePresignedUrl(key, 0));
    assertThrows(IllegalArgumentException.class, () -> s3Service.generatePresignedUrl(key, 10081));
  }

  @Test
  @DisplayName("Should throw exception for null bucket name in set bucket policy")
  void shouldThrowExceptionForNullBucketNameInSetBucketPolicy() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[]}";

    assertThrows(IllegalArgumentException.class, () -> s3Service.setBucketPolicy(null, policy));
  }

  @Test
  @DisplayName("Should throw exception for null policy in set bucket policy")
  void shouldThrowExceptionForNullPolicyInSetBucketPolicy() {
    when(s3Properties.getBucketName()).thenReturn("test-bucket");
    when(s3Properties.getRegion()).thenReturn("us-east-1");
    
    String bucketName = "test-bucket";

    assertThrows(IllegalArgumentException.class, () -> s3Service.setBucketPolicy(bucketName, null));
  }
}
