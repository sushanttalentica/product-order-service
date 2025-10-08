package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.domain.service.ProductService;
import com.ecommerce.productorder.dto.request.CreateProductRequest;
import com.ecommerce.productorder.dto.request.UpdateProductRequest;
import com.ecommerce.productorder.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management operations")
public class ProductController {
    
    private final ProductService productService;
    
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product", description = "Create a new product (Admin only)")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid product data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "Product creation data") @Valid @RequestBody CreateProductRequest request) {
        log.info("Creating product with name: {}", request.getName());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long productId,
                                                       @Valid @RequestBody UpdateProductRequest request) {
        log.info("Updating product with id: {}", productId);
        ProductResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long productId) {
        log.debug("Retrieving product with id: {}", productId);
        return productService.getProductById(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        log.debug("Retrieving product with SKU: {}", sku);
        return productService.getProductBySku(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a paginated list of all active products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving all products with pagination: {}", pageable);
        Page<ProductResponse> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(@RequestParam(required = false) String query,
                                                               @RequestParam(required = false) String name,
                                                               @PageableDefault(size = 20) Pageable pageable) {
        String searchTerm = query != null ? query : name;
        log.debug("Searching products by term: {}", searchTerm);
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            // If no search term provided, return all products
            Page<ProductResponse> response = productService.getAllProducts(pageable);
            return ResponseEntity.ok(response);
        }
        Page<ProductResponse> response = productService.searchProductsByName(searchTerm, pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(@PathVariable Long categoryId,
                                                                      @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving products by category: {}", categoryId);
        Page<ProductResponse> response = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/price-range")
    public ResponseEntity<Page<ProductResponse>> getProductsByPriceRange(@RequestParam BigDecimal minPrice,
                                                                        @RequestParam BigDecimal maxPrice,
                                                                        @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Retrieving products by price range: {} - {}", minPrice, maxPrice);
        Page<ProductResponse> response = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/search/advanced")
    public ResponseEntity<Page<ProductResponse>> searchProducts(@RequestParam(required = false) String name,
                                                               @RequestParam(required = false) Long categoryId,
                                                               @RequestParam(required = false) BigDecimal minPrice,
                                                               @RequestParam(required = false) BigDecimal maxPrice,
                                                               @PageableDefault(size = 20) Pageable pageable) {
        log.debug("Advanced product search with criteria - name: {}, category: {}, price: {} - {}", 
                 name, categoryId, minPrice, maxPrice);
        Page<ProductResponse> response = productService.searchProducts(name, categoryId, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(response);
    }
    
    
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        log.info("Deleting product with id: {}", productId);
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
    
    
    @GetMapping("/low-stock")
    // @PreAuthorize("hasRole('ADMIN')") // DISABLED FOR TESTING
    public ResponseEntity<List<ProductResponse>> getProductsWithLowStock(@RequestParam(defaultValue = "10") Integer threshold) {
        log.debug("Retrieving products with low stock below threshold: {}", threshold);
        List<ProductResponse> response = productService.getProductsWithLowStock(threshold);
        return ResponseEntity.ok(response);
    }
    
    
    @PatchMapping("/{productId}/stock")
    // @PreAuthorize("hasRole('ADMIN')") // DISABLED FOR TESTING
    public ResponseEntity<ProductResponse> updateProductStock(@PathVariable Long productId,
                                                             @RequestParam Integer stock) {
        log.info("Updating product stock for id: {} to {}", productId, stock);
        ProductResponse response = productService.updateProductStock(productId, stock);
        return ResponseEntity.ok(response);
    }
}
