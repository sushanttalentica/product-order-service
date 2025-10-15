package com.ecommerce.productorder.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.topics")
@Getter
@Setter
public class KafkaTopicsProperties {

  private Order order = new Order();
  private Payment payment = new Payment();
  private Product product = new Product();

  @Getter
  @Setter
  public static class Order {
    private String created;
    private String statusUpdated;
    private String cancelled;
    private String completed;
  }

  @Getter
  @Setter
  public static class Payment {
    private String processed;
    private String failed;
    private String refunded;
    private String cancelled;
    private String retry;
  }

  @Getter
  @Setter
  public static class Product {
    private String stockUpdated;
  }
}
