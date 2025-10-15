package com.ecommerce.productorder.domain.service;

import com.ecommerce.productorder.dto.request.CreateProductRequest;
import com.ecommerce.productorder.dto.request.UpdateProductRequest;
import com.ecommerce.productorder.dto.response.ProductResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

  ProductResponse createProduct(CreateProductRequest request);

  ProductResponse updateProduct(Long productId, UpdateProductRequest request);

  Optional<ProductResponse> getProductById(Long productId);

  Optional<ProductResponse> getProductBySku(String sku);

  Page<ProductResponse> getAllProducts(Pageable pageable);

  Page<ProductResponse> searchProductsByName(String name, Pageable pageable);

  Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);

  Page<ProductResponse> getProductsByPriceRange(
      java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice, Pageable pageable);

  Page<ProductResponse> searchProducts(
      String name,
      Long categoryId,
      java.math.BigDecimal minPrice,
      java.math.BigDecimal maxPrice,
      Pageable pageable);

  void deleteProduct(Long productId);

  List<ProductResponse> getProductsWithLowStock(Integer threshold);

  ProductResponse updateProductStock(Long productId, Integer newStock);

  void reduceProductStock(Long productId, Integer quantity);

  void restoreProductStock(Long productId, Integer quantity);
}
