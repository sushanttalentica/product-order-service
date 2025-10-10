package com.ecommerce.productorder.domain.service;

import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.dto.request.CreateProductRequest;
import com.ecommerce.productorder.dto.request.UpdateProductRequest;
import com.ecommerce.productorder.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    

    ProductResponse createProduct(CreateProductRequest request);
    

    ProductResponse updateProduct(Long productId, UpdateProductRequest request);
    

    Optional<ProductResponse> getProductById(Long productId);
    

    Optional<ProductResponse> getProductBySku(String sku);
    

    Page<ProductResponse> getAllProducts(Pageable pageable);
    
    /**
     * Searches products by name
     */
    Page<ProductResponse> searchProductsByName(String name, Pageable pageable);
    
    /**
     * Searches products by category
     */
    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);
    
    /**
     * Searches products by price range
     */
    Page<ProductResponse> getProductsByPriceRange(java.math.BigDecimal minPrice, 
                                                java.math.BigDecimal maxPrice, 
                                                Pageable pageable);
    
    /**
     * Advanced product search with multiple criteria
     */
    Page<ProductResponse> searchProducts(String name, 
                                       Long categoryId, 
                                       java.math.BigDecimal minPrice, 
                                       java.math.BigDecimal maxPrice, 
                                       Pageable pageable);
    

    void deleteProduct(Long productId);
    
    /**
     * Gets products with low stock
     */
    List<ProductResponse> getProductsWithLowStock(Integer threshold);
    

    ProductResponse updateProductStock(Long productId, Integer newStock);
    
    /**
     * Reduces product stock (for order processing)
     */
    void reduceProductStock(Long productId, Integer quantity);
    
    /**
     * Restores product stock (for order cancellation)
     */
    void restoreProductStock(Long productId, Integer quantity);
}
