package com.ecommerce.productorder.mapper;

import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.dto.request.CreateProductRequest;
import com.ecommerce.productorder.dto.request.UpdateProductRequest;
import com.ecommerce.productorder.dto.response.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request) {
        if (request == null) {
            return null;
        }
        
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .sku(request.getSku())
                .isActive(true)
                .build();
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }

        ProductResponse.CategoryResponse categoryResponse = null;
        if (product.getCategory() != null) {
            categoryResponse = new ProductResponse.CategoryResponse(
                    product.getCategory().getId(),
                    product.getCategory().getName(),
                    product.getCategory().getDescription(),
                    product.getCategory().getIsActive()
            );
        }

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getSku(),
                product.getIsActive(),
                categoryResponse,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public Product createUpdatedProduct(Product existingProduct, UpdateProductRequest request) {
        if (existingProduct == null || request == null) {
            return existingProduct;
        }

        return Product.builder()
                .id(existingProduct.getId())
                .name(request.getName() != null ? request.getName() : existingProduct.getName())
                .description(request.getDescription() != null ? request.getDescription() : existingProduct.getDescription())
                .price(request.getPrice() != null ? request.getPrice() : existingProduct.getPrice())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : existingProduct.getStockQuantity())
                .sku(existingProduct.getSku())
                .isActive(request.getIsActive() != null ? request.getIsActive() : existingProduct.getIsActive())
                .category(existingProduct.getCategory())
                .version(existingProduct.getVersion())
                .createdAt(existingProduct.getCreatedAt())
                .build();
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
