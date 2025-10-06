package com.ecommerce.productorder.domain.service;

import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.dto.request.CreateProductRequest;
import com.ecommerce.productorder.dto.request.UpdateProductRequest;
import com.ecommerce.productorder.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Product business operations
 * 
 * Design Principles Applied:
 * - Service Pattern: Encapsulates business logic
 * - Interface Segregation: Only exposes necessary business operations
 * - Dependency Inversion: Depends on abstraction, not concrete implementation
 * - Single Responsibility: Only handles Product business operations
 * - Facade Pattern: Provides simplified interface to complex operations
 */
public interface ProductService {
    
    /**
     * Creates a new product
     * Encapsulates product creation business logic
     */
    ProductResponse createProduct(CreateProductRequest request);
    
    /**
     * Updates an existing product
     * Encapsulates product update business logic
     */
    ProductResponse updateProduct(Long productId, UpdateProductRequest request);
    
    /**
     * Retrieves a product by ID
     * Encapsulates product retrieval business logic
     */
    Optional<ProductResponse> getProductById(Long productId);
    
    /**
     * Retrieves a product by SKU
     * Encapsulates product retrieval by SKU business logic
     */
    Optional<ProductResponse> getProductBySku(String sku);
    
    /**
     * Retrieves all products with pagination
     * Encapsulates product listing business logic
     */
    Page<ProductResponse> getAllProducts(Pageable pageable);
    
    /**
     * Searches products by name
     * Encapsulates product search business logic
     */
    Page<ProductResponse> searchProductsByName(String name, Pageable pageable);
    
    /**
     * Searches products by category
     * Encapsulates product search by category business logic
     */
    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);
    
    /**
     * Searches products by price range
     * Encapsulates product search by price business logic
     */
    Page<ProductResponse> getProductsByPriceRange(java.math.BigDecimal minPrice, 
                                                java.math.BigDecimal maxPrice, 
                                                Pageable pageable);
    
    /**
     * Advanced product search with multiple criteria
     * Encapsulates complex product search business logic
     */
    Page<ProductResponse> searchProducts(String name, 
                                       Long categoryId, 
                                       java.math.BigDecimal minPrice, 
                                       java.math.BigDecimal maxPrice, 
                                       Pageable pageable);
    
    /**
     * Deletes a product (soft delete)
     * Encapsulates product deletion business logic
     */
    void deleteProduct(Long productId);
    
    /**
     * Gets products with low stock
     * Encapsulates inventory management business logic
     */
    List<ProductResponse> getProductsWithLowStock(Integer threshold);
    
    /**
     * Updates product stock
     * Encapsulates inventory update business logic
     */
    ProductResponse updateProductStock(Long productId, Integer newStock);
    
    /**
     * Reduces product stock (for order processing)
     * Encapsulates stock reduction business logic
     */
    void reduceProductStock(Long productId, Integer quantity);
    
    /**
     * Restores product stock (for order cancellation)
     * Encapsulates stock restoration business logic
     */
    void restoreProductStock(Long productId, Integer quantity);
}
