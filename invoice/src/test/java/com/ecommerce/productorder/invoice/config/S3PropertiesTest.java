package com.ecommerce.productorder.invoice.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// JUnit tests for S3Properties configuration class.
@DisplayName("S3Properties Tests")
public class S3PropertiesTest {

  private S3Properties s3Properties;

  @BeforeEach
  void setUp() {
    s3Properties = new S3Properties();
  }

  @Test
  @DisplayName("Should create S3Properties with default values")
  void shouldCreateS3PropertiesWithDefaultValues() {
    assertNotNull(s3Properties);
    assertNull(s3Properties.getBucketName());
    assertNull(s3Properties.getRegion());
    assertNull(s3Properties.getAccessKey());
    assertNull(s3Properties.getSecretKey());
  }

  @Test
  @DisplayName("Should set and get bucket name")
  void shouldSetAndGetBucketName() {
    String bucketName = "test-bucket";
    s3Properties.setBucketName(bucketName);
    assertEquals(bucketName, s3Properties.getBucketName());
  }

  @Test
  @DisplayName("Should set and get region")
  void shouldSetAndGetRegion() {
    String region = "us-east-1";
    s3Properties.setRegion(region);
    assertEquals(region, s3Properties.getRegion());
  }

  @Test
  @DisplayName("Should set and get access key")
  void shouldSetAndGetAccessKey() {
    String accessKey = "test-access-key";
    s3Properties.setAccessKey(accessKey);
    assertEquals(accessKey, s3Properties.getAccessKey());
  }

  @Test
  @DisplayName("Should set and get secret key")
  void shouldSetAndGetSecretKey() {
    String secretKey = "test-secret-key";
    s3Properties.setSecretKey(secretKey);
    assertEquals(secretKey, s3Properties.getSecretKey());
  }

  @Test
  @DisplayName("Should handle null values")
  void shouldHandleNullValues() {
    s3Properties.setBucketName(null);
    s3Properties.setRegion(null);
    s3Properties.setAccessKey(null);
    s3Properties.setSecretKey(null);

    assertNull(s3Properties.getBucketName());
    assertNull(s3Properties.getRegion());
    assertNull(s3Properties.getAccessKey());
    assertNull(s3Properties.getSecretKey());
  }

  @Test
  @DisplayName("Should handle empty strings")
  void shouldHandleEmptyStrings() {
    s3Properties.setBucketName("");
    s3Properties.setRegion("");
    s3Properties.setAccessKey("");
    s3Properties.setSecretKey("");

    assertEquals("", s3Properties.getBucketName());
    assertEquals("", s3Properties.getRegion());
    assertEquals("", s3Properties.getAccessKey());
    assertEquals("", s3Properties.getSecretKey());
  }
}
