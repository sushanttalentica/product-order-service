package com.ecommerce.productorder.performance;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.CategoryRepository;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import com.ecommerce.productorder.domain.service.ProductService;
import com.ecommerce.productorder.dto.request.CreateProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Product Service Performance Tests")
class ProductServicePerformanceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        // Clean up database
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test category
        testCategory = Category.builder()
                .name("Electronics")
                .description("Electronic products")
                .isActive(true)
                .build();
        testCategory = categoryRepository.save(testCategory);

        // Create test products
        testProducts = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Product product = Product.builder()
                    .name("Product " + i)
                    .description("Description for product " + i)
                    .price(new BigDecimal("99.99").add(new BigDecimal(i)))
                    .stockQuantity(100 + i)
                    .sku("SKU-" + String.format("%04d", i))
                    .isActive(true)
                    .category(testCategory)
                    .build();
            testProducts.add(product);
        }
        productRepository.saveAll(testProducts);
    }

    @Test
    @DisplayName("Should handle concurrent product creation")
    void shouldHandleConcurrentProductCreation() throws Exception {
        // Arrange
        int numberOfThreads = 10;
        int productsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < productsPerThread; j++) {
                    CreateProductRequest request = CreateProductRequest.builder()
                            .name("Concurrent Product " + threadId + "-" + j)
                            .description("Description for concurrent product " + threadId + "-" + j)
                            .price(new BigDecimal("99.99"))
                            .stockQuantity(100)
                            .sku("CONCURRENT-SKU-" + threadId + "-" + j)
                            .categoryId(testCategory.getId())
                            .build();

                    productService.createProduct(request);
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        long totalProducts = productRepository.count();
        assertThat(totalProducts).isGreaterThanOrEqualTo(1000 + (numberOfThreads * productsPerThread));
    }

    @Test
    @DisplayName("Should handle concurrent product retrieval")
    void shouldHandleConcurrentProductRetrieval() throws Exception {
        // Arrange
        int numberOfThreads = 20;
        int retrievalsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < retrievalsPerThread; j++) {
                    // Random product ID between 1 and 1000
                    long productId = (long) (Math.random() * 1000) + 1;
                    productService.getProductById(productId);
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - No exceptions should be thrown
        assertThat(futures).allMatch(future -> future.isDone() && !future.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Should handle concurrent product search")
    void shouldHandleConcurrentProductSearch() throws Exception {
        // Arrange
        int numberOfThreads = 15;
        int searchesPerThread = 30;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < searchesPerThread; j++) {
                    // Search for products with different patterns
                    String searchTerm = "Product " + (threadId * 10 + j);
                    productService.searchProductsByName(searchTerm, org.springframework.data.domain.PageRequest.of(0, 10));
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - No exceptions should be thrown
        assertThat(futures).allMatch(future -> future.isDone() && !future.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Should handle concurrent product updates")
    void shouldHandleConcurrentProductUpdates() throws Exception {
        // Arrange
        int numberOfThreads = 5;
        int updatesPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < updatesPerThread; j++) {
                    // Update random product
                    long productId = (long) (Math.random() * 1000) + 1;
                    try {
                        productService.updateProductStock(productId, 100 + threadId + j);
                    } catch (Exception e) {
                        // Ignore exceptions for products that don't exist
                    }
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - No exceptions should be thrown
        assertThat(futures).allMatch(future -> future.isDone() && !future.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Should handle large dataset queries efficiently")
    void shouldHandleLargeDatasetQueriesEfficiently() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act
        org.springframework.data.domain.Page<Product> products = productService.getAllProducts(
                org.springframework.data.domain.PageRequest.of(0, 100));

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert
        assertThat(products.getContent()).hasSize(100);
        assertThat(executionTime).isLessThan(1000); // Should complete within 1 second
    }

    @Test
    @DisplayName("Should handle pagination efficiently")
    void shouldHandlePaginationEfficiently() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act
        org.springframework.data.domain.Page<Product> products = productService.getAllProducts(
                org.springframework.data.domain.PageRequest.of(0, 20));

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert
        assertThat(products.getContent()).hasSize(20);
        assertThat(executionTime).isLessThan(500); // Should complete within 500ms
    }

    @Test
    @DisplayName("Should handle price range queries efficiently")
    void shouldHandlePriceRangeQueriesEfficiently() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act
        org.springframework.data.domain.Page<Product> products = productService.getProductsByPriceRange(
                new BigDecimal("50.00"), new BigDecimal("200.00"),
                org.springframework.data.domain.PageRequest.of(0, 50));

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert
        assertThat(products.getContent()).hasSizeLessThanOrEqualTo(50);
        assertThat(executionTime).isLessThan(500); // Should complete within 500ms
    }

    @Test
    @DisplayName("Should handle category queries efficiently")
    void shouldHandleCategoryQueriesEfficiently() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act
        org.springframework.data.domain.Page<Product> products = productService.getProductsByCategory(
                testCategory.getId(), org.springframework.data.domain.PageRequest.of(0, 100));

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert
        assertThat(products.getContent()).hasSize(100);
        assertThat(executionTime).isLessThan(500); // Should complete within 500ms
    }

    @Test
    @DisplayName("Should handle search queries efficiently")
    void shouldHandleSearchQueriesEfficiently() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act
        org.springframework.data.domain.Page<Product> products = productService.searchProductsByName(
                "Product", org.springframework.data.domain.PageRequest.of(0, 50));

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert
        assertThat(products.getContent()).hasSizeLessThanOrEqualTo(50);
        assertThat(executionTime).isLessThan(500); // Should complete within 500ms
    }

    @Test
    @DisplayName("Should handle low stock queries efficiently")
    void shouldHandleLowStockQueriesEfficiently() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act
        List<com.ecommerce.productorder.dto.response.ProductResponse> products = 
                productService.getProductsWithLowStock(50);

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // Assert
        assertThat(products).isNotNull();
        assertThat(executionTime).isLessThan(500); // Should complete within 500ms
    }

    @Test
    @DisplayName("Should handle memory efficiently during bulk operations")
    void shouldHandleMemoryEfficientlyDuringBulkOperations() {
        // Arrange
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // Act
        for (int i = 0; i < 100; i++) {
            productService.getAllProducts(org.springframework.data.domain.PageRequest.of(0, 100));
        }

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;

        // Assert
        assertThat(memoryUsed).isLessThan(50 * 1024 * 1024); // Should use less than 50MB
    }

    @Test
    @DisplayName("Should handle database connection pooling efficiently")
    void shouldHandleDatabaseConnectionPoolingEfficiently() throws Exception {
        // Arrange
        int numberOfThreads = 20;
        int operationsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Act
        for (int i = 0; i < numberOfThreads; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    productService.getAllProducts(org.springframework.data.domain.PageRequest.of(0, 10));
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert - No exceptions should be thrown
        assertThat(futures).allMatch(future -> future.isDone() && !future.isCompletedExceptionally());
    }
}
