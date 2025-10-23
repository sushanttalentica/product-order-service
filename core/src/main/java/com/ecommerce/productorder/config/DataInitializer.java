package com.ecommerce.productorder.config;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.CategoryRepository;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * CommandLineRunner for demo/dummy data generation. Creates sample categories and products for
 * testing and demonstration purposes. This generates dummy data to make the application immediately
 * usable for demos.
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;

  public DataInitializer(
      CategoryRepository categoryRepository, ProductRepository productRepository) {
    this.categoryRepository = categoryRepository;
    this.productRepository = productRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    log.info("=== DATA INITIALIZER STARTING ===");
    log.info("Starting data initialization...");
    log.info("Initializing test data...");

    // Always create test data for testing (dummy data)
    log.info("Creating test categories and products...");
    Category electronics = new Category();
    electronics.setName("Electronics");
    electronics.setDescription("Electronic devices and gadgets");
    electronics.setActive(true);
    categoryRepository.save(electronics);
    log.info("Created Electronics category");

    Category clothing = new Category();
    clothing.setName("Clothing");
    clothing.setDescription("Fashion and apparel");
    clothing.setActive(true);
    categoryRepository.save(clothing);
    log.info("Created Clothing category");

    Category books = new Category();
    books.setName("Books");
    books.setDescription("Books and literature");
    books.setActive(true);
    categoryRepository.save(books);
    log.info("Created Books category");

    // Create products (always create for testing - dummy data)
    log.info("Creating products...");
    Product iphone = new Product();
    iphone.setName("iPhone 15");
    iphone.setDescription("Latest Apple smartphone");
    iphone.setPrice(new BigDecimal("999.99"));
    iphone.setStockQuantity(50);
    iphone.setSku("IPHONE15-001");
    iphone.setActive(true);
    iphone.setCategory(electronics);
    productRepository.save(iphone);
    log.info("Created iPhone 15 product");

    Product macbook = new Product();
    macbook.setName("MacBook Pro");
    macbook.setDescription("Apple laptop computer");
    macbook.setPrice(new BigDecimal("1999.99"));
    macbook.setStockQuantity(25);
    macbook.setSku("MBP-001");
    macbook.setActive(true);
    macbook.setCategory(electronics);
    productRepository.save(macbook);
    log.info("Created MacBook Pro product");

    Product tshirt = new Product();
    tshirt.setName("T-Shirt");
    tshirt.setDescription("Cotton t-shirt");
    tshirt.setPrice(new BigDecimal("29.99"));
    tshirt.setStockQuantity(100);
    tshirt.setSku("TSHIRT-001");
    tshirt.setActive(true);
    tshirt.setCategory(clothing);
    productRepository.save(tshirt);
    log.info("Created T-Shirt product");

    Product book = new Product();
    book.setName("Programming Book");
    book.setDescription("Learn Java programming");
    book.setPrice(new BigDecimal("49.99"));
    book.setStockQuantity(75);
    book.setSku("BOOK-001");
    book.setActive(true);
    book.setCategory(books);
    productRepository.save(book);
    log.info("Created Programming Book product");

    log.info("Test data initialization completed");
  }
}
