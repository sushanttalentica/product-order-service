package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.api.ProductsApi;
import com.ecommerce.productorder.api.model.CreateProductRequest;
import com.ecommerce.productorder.api.model.MessageResponse;
import com.ecommerce.productorder.api.model.ProductResponse;
import com.ecommerce.productorder.api.model.UpdateProductRequest;
import com.ecommerce.productorder.api.model.UpdateStockRequest;
import com.ecommerce.productorder.domain.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class ProductsApiImpl implements ProductsApi {

    private final ProductService productService;    
    public ProductsApiImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public ResponseEntity<ProductResponse> createProduct(CreateProductRequest createProductRequest) {
        log.info("Creating product: {}", createProductRequest.getName());
        var dtoRequest = com.ecommerce.productorder.dto.request.CreateProductRequest.builder()
                .name(createProductRequest.getName())
                .description(createProductRequest.getDescription())
                .price(createProductRequest.getPrice())
                .stockQuantity(createProductRequest.getStockQuantity())
                .sku(createProductRequest.getSku())
                .categoryId(createProductRequest.getCategoryId())
                .build();
        
        var response = productService.createProduct(dtoRequest);
        return ResponseEntity.status(201).body(convertToApiModel(response));
    }

    @Override
    public ResponseEntity<Object> getAllProducts(Integer page, Integer size) {
        Page<com.ecommerce.productorder.dto.response.ProductResponse> productsPage = 
                productService.getAllProducts(PageRequest.of(page, size));
        return ResponseEntity.ok(productsPage);
    }

    @Override
    public ResponseEntity<ProductResponse> getProductById(Long productId) {
        return productService.getProductById(productId)
                .map(this::convertToApiModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(null));
    }

    @Override
    public ResponseEntity<ProductResponse> updateProduct(Long productId, UpdateProductRequest updateProductRequest) {
        log.info("Updating product: {}", productId);
        var dtoRequest = com.ecommerce.productorder.dto.request.UpdateProductRequest.builder()
                .name(updateProductRequest.getName())
                .description(updateProductRequest.getDescription())
                .price(updateProductRequest.getPrice())
                .stockQuantity(updateProductRequest.getStockQuantity())
                .isActive(updateProductRequest.getIsActive())
                .categoryId(updateProductRequest.getCategoryId())
                .build();
        
        var response = productService.updateProduct(productId, dtoRequest);
        return ResponseEntity.ok(convertToApiModel(response));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteProduct(Long productId) {
        log.info("Deleting product: {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.ok(new MessageResponse()
                .message("Product deleted successfully")
                .success(true));
    }

    @Override
    public ResponseEntity<ProductResponse> getProductBySku(String sku) {
        return productService.getProductBySku(sku)
                .map(this::convertToApiModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(null));
    }

    @Override
    public ResponseEntity<List<ProductResponse>> searchProducts(String keyword) {
        var products = productService.searchProductsByName(keyword, PageRequest.of(0, 100));
        return ResponseEntity.ok(products.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(Long categoryId) {
        var products = productService.getProductsByCategory(categoryId, PageRequest.of(0, 100));
        return ResponseEntity.ok(products.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<List<ProductResponse>> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        var products = productService.getProductsByPriceRange(minPrice, maxPrice, PageRequest.of(0, 100));
        return ResponseEntity.ok(products.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<List<ProductResponse>> advancedSearchProducts(
            String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Boolean isActive) {
        var products = productService.searchProducts(name, categoryId, minPrice, maxPrice, PageRequest.of(0, 100));
        return ResponseEntity.ok(products.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<List<ProductResponse>> getLowStockProducts(Integer threshold) {
        var products = productService.getProductsWithLowStock(threshold);
        return ResponseEntity.ok(products.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<ProductResponse> updateProductStock(Long productId, UpdateStockRequest updateStockRequest) {
        log.info("Updating stock for product: {}", productId);
        var response = productService.updateProductStock(productId, updateStockRequest.getStockQuantity());
        return ResponseEntity.ok(convertToApiModel(response));
    }

    private ProductResponse convertToApiModel(com.ecommerce.productorder.dto.response.ProductResponse dto) {
        var apiModel = new ProductResponse();
        apiModel.setId(dto.id());
        apiModel.setName(dto.name());
        apiModel.setDescription(dto.description());
        apiModel.setPrice(dto.price());
        apiModel.setStockQuantity(dto.stockQuantity());
        apiModel.setSku(dto.sku());
        apiModel.setIsActive(dto.isActive());
        apiModel.setCreatedAt(dto.createdAt() != null ? dto.createdAt().atOffset(ZoneOffset.UTC) : null);
        apiModel.setUpdatedAt(dto.updatedAt() != null ? dto.updatedAt().atOffset(ZoneOffset.UTC) : null);
        
        if (dto.category() != null) {
            var category = new com.ecommerce.productorder.api.model.CategoryResponse();
            category.setId(dto.category().id());
            category.setName(dto.category().name());
            category.setDescription(dto.category().description());
            category.setIsActive(dto.category().isActive());
            apiModel.setCategory(category);
        }
        
        return apiModel;
    }
}

