package com.ecommerce.productorder.domain.service.impl;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.CategoryRepository;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import com.ecommerce.productorder.domain.service.ProductService;
import com.ecommerce.productorder.dto.request.CreateProductRequest;
import com.ecommerce.productorder.dto.request.UpdateProductRequest;
import com.ecommerce.productorder.dto.response.ProductResponse;
import com.ecommerce.productorder.exception.BusinessException;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import com.ecommerce.productorder.mapper.ProductMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {
    
    private static final String STOCK_UPDATE_TOPIC = "product.stock.updated";
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public ProductServiceImpl(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             ProductMapper productMapper,
                             KafkaTemplate<String, Object> kafkaTemplate) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with name: {}", request.getName());
        
        // Business rule: Check if SKU already exists
        if (productRepository.existsBySkuAndIsActiveTrue(request.getSku())) {
            throw new BusinessException("Product with SKU " + request.getSku() + " already exists");
        }
        
        // Validate category exists and is active
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
        
        if (!category.isAvailableForProducts()) {
            throw new BusinessException("Category is not active for products");
        }
        
        // Create product entity
        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        
        // Save product
        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with id: {}", savedProduct.getId());
        
        return productMapper.toResponse(savedProduct);
    }
    
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        log.info("Updating product with id: {}", productId);
        
        // Product must exist
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Check SKU uniqueness if SKU is being updated
        if (request.getSku() != null && !request.getSku().equals(product.getSku())) {
            if (productRepository.existsBySkuAndIsActiveTrue(request.getSku())) {
                throw new BusinessException("Product with SKU " + request.getSku() + " already exists");
            }
        }
        
        Product updatedProduct = productMapper.createUpdatedProduct(product, request);
        
        if (request.getCategoryId() != null && !request.getCategoryId().equals(product.getCategory().getId())) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            
            if (!category.isAvailableForProducts()) {
                throw new BusinessException("Category is not active for products");
            }
            updatedProduct.setCategory(category);
        }
        
        updatedProduct = productRepository.save(updatedProduct);
        log.info("Product updated successfully with id: {}", updatedProduct.getId());
        
        return productMapper.toResponse(updatedProduct);
    }
    
    @Override
    @Cacheable(value = "products", key = "#productId")
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductById(Long productId) {
        log.debug("Retrieving product with id: {}", productId);
        
        return productRepository.findById(productId)
                .filter(Product::isAvailableForPurchase)
                .map(productMapper::toResponse);
    }
    
    @Override
    @Cacheable(value = "products", key = "'sku:' + #sku")
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductBySku(String sku) {
        log.debug("Retrieving product with SKU: {}", sku);
        
        return productRepository.findBySkuAndIsActiveTrue(sku)
                .map(productMapper::toResponse);
    }
    
    @Override
    @Cacheable(value = "products", key = "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.debug("Retrieving all products with pagination: {}", pageable);
        
        return productRepository.findByIsActiveTrue(pageable)
                .map(productMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProductsByName(String name, Pageable pageable) {
        log.debug("Searching products by name: {}", name);
        
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name, pageable)
                .map(productMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        log.debug("Retrieving products by category: {}", categoryId);
        
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable)
                .map(productMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByPriceRange(java.math.BigDecimal minPrice, 
                                                       java.math.BigDecimal maxPrice, 
                                                       Pageable pageable) {
        log.debug("Retrieving products by price range: {} - {}", minPrice, maxPrice);
        
        return productRepository.findByPriceBetweenAndIsActiveTrue(minPrice, maxPrice, pageable)
                .map(productMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, 
                                              Long categoryId, 
                                              java.math.BigDecimal minPrice, 
                                              java.math.BigDecimal maxPrice, 
                                              Pageable pageable) {
        log.debug("Advanced product search with criteria - name: {}, category: {}, price: {} - {}", 
                 name, categoryId, minPrice, maxPrice);
        
        return productRepository.findProductsByCriteria(name, categoryId, minPrice, maxPrice, pageable)
                .map(productMapper::toResponse);
    }
    
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long productId) {
        log.info("Deleting product with id: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Soft delete - set isActive to false
        product.setIsActive(false);
        productRepository.save(product);
        
        log.info("Product deleted successfully with id: {}", productId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsWithLowStock(Integer threshold) {
        log.debug("Retrieving products with low stock below threshold: {}", threshold);
        
        return productRepository.findProductsWithLowStock(threshold)
                .stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @CacheEvict(value = "products", key = "#productId")
    @CachePut(value = "products", key = "#result.id")
    public ProductResponse updateProductStock(Long productId, Integer newStock) {
        log.info("Updating product stock for id: {} to {}", productId, newStock);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        if (newStock < 0) {
            throw new BusinessException("Stock quantity cannot be negative");
        }
        
        product.setStockQuantity(newStock);
        Product updatedProduct = productRepository.save(product);
        
        // Publish stock update event for real-time broadcasting
        publishStockUpdateEvent(updatedProduct);
        
        log.info("Product stock updated successfully for id: {}", productId);
        return productMapper.toResponse(updatedProduct);
    }
    
    @Override
    @CacheEvict(value = "products", key = "#productId")
    public void reduceProductStock(Long productId, Integer quantity) {
        log.info("Reducing product stock for id: {} by {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be positive");
        }
        
        // Use business method from entity
        product.reduceStock(quantity);
        Product updatedProduct = productRepository.save(product);
        
        // Publish stock update event for real-time broadcasting
        publishStockUpdateEvent(updatedProduct);
        
        log.info("Product stock reduced successfully for id: {}", productId);
    }
    

    private void publishStockUpdateEvent(Product product) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("productId", product.getId());
            event.put("productName", product.getName());
            event.put("stockQuantity", product.getStockQuantity());
            event.put("timestamp", System.currentTimeMillis());
            event.put("eventType", "STOCK_UPDATED");
            
            kafkaTemplate.send(STOCK_UPDATE_TOPIC, product.getId().toString(), event);
            log.debug("Stock update event published for product ID: {}", product.getId());
        } catch (Exception e) {
            log.error("Failed to publish stock update event for product ID: {}", product.getId(), e);
            // Don't throw - stock update broadcast is not critical for order flow
        }
    }
    
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void restoreProductStock(Long productId, Integer quantity) {
        log.info("Restoring product stock for id: {} by {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be positive");
        }
        
        // Use business method from entity
        product.restoreStock(quantity);
        productRepository.save(product);
        
        log.info("Product stock restored successfully for id: {}", productId);
    }
}
