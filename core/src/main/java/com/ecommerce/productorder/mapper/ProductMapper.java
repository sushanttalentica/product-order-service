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
        
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setIsActive(true);
        return product;
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

        Product product = new Product();
        product.setId(existingProduct.getId());
        product.setName(request.getName() != null ? request.getName() : existingProduct.getName());
        product.setDescription(request.getDescription() != null ? request.getDescription() : existingProduct.getDescription());
        product.setPrice(request.getPrice() != null ? request.getPrice() : existingProduct.getPrice());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : existingProduct.getStockQuantity());
        product.setSku(existingProduct.getSku());
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : existingProduct.getIsActive());
        product.setCategory(existingProduct.getCategory());
        product.setVersion(existingProduct.getVersion());
        product.setCreatedAt(existingProduct.getCreatedAt());
        return product;
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
