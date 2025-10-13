package com.ecommerce.productorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableCaching // Enables Spring's caching abstraction for in-memory caching
@EnableKafka // Enables Kafka message processing capabilities
public class ProductOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductOrderServiceApplication.class, args);
    }
}
