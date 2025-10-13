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

    public void updateEntity(Product product, UpdateProductRequest request) {
        if (product == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }
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
