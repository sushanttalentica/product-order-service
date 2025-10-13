package com.ecommerce.productorder.config;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.CategoryRepository;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Data initializer for test data
 * Creates sample categories and products for testing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== DATA INITIALIZER STARTING ===");
        log.info("Starting data initialization...");
        log.info("Initializing test data...");
        
        // Always create test data for testing
        log.info("Creating test categories and products...");
            Category electronics = Category.builder()
                    .name("Electronics")
                    .description("Electronic devices and gadgets")
                    .isActive(true)
                    .build();
            categoryRepository.save(electronics);
            log.info("Created Electronics category");

            Category clothing = Category.builder()
                    .name("Clothing")
                    .description("Fashion and apparel")
                    .isActive(true)
                    .build();
            categoryRepository.save(clothing);
            log.info("Created Clothing category");

            Category books = Category.builder()
                    .name("Books")
                    .description("Books and literature")
                    .isActive(true)
                    .build();
            categoryRepository.save(books);
            log.info("Created Books category");

            // Create products (always create for testing)
            log.info("Creating products...");
                Product iphone = Product.builder()
                        .name("iPhone 15")
                        .description("Latest Apple smartphone")
                        .price(new BigDecimal("999.99"))
                        .stockQuantity(50)
                        .sku("IPHONE15-001")
                        .isActive(true)
                        .category(electronics)
                        .build();
                productRepository.save(iphone);
                log.info("Created iPhone 15 product");

                Product macbook = Product.builder()
                        .name("MacBook Pro")
                        .description("Apple laptop computer")
                        .price(new BigDecimal("1999.99"))
                        .stockQuantity(25)
                        .sku("MBP-001")
                        .isActive(true)
                        .category(electronics)
                        .build();
                productRepository.save(macbook);
                log.info("Created MacBook Pro product");

                Product tshirt = Product.builder()
                        .name("T-Shirt")
                        .description("Cotton t-shirt")
                        .price(new BigDecimal("29.99"))
                        .stockQuantity(100)
                        .sku("TSHIRT-001")
                        .isActive(true)
                        .category(clothing)
                        .build();
                productRepository.save(tshirt);
                log.info("Created T-Shirt product");

                Product book = Product.builder()
                        .name("Programming Book")
                        .description("Learn Java programming")
                        .price(new BigDecimal("49.99"))
                        .stockQuantity(75)
                        .sku("BOOK-001")
                        .isActive(true)
                        .category(books)
                        .build();
                productRepository.save(book);
                log.info("Created Programming Book product");
        
        log.info("Test data initialization completed");
    }
}
