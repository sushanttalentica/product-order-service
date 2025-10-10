package com.ecommerce.productorder.mapper;

import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.dto.request.CreateProductRequest;
import com.ecommerce.productorder.dto.request.UpdateProductRequest;
import com.ecommerce.productorder.dto.response.ProductResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    
    /**
     * Maps CreateProductRequest to Product entity
     * Uses MapStruct for automatic mapping with custom logic
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    Product toEntity(CreateProductRequest request);
    
    /**
     * Maps Product entity to ProductResponse DTO
     * Uses MapStruct for automatic mapping
     */
    ProductResponse toResponse(Product product);
    
    /**
     * Updates Product entity from UpdateProductRequest
     * Uses MapStruct for selective updates
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Product product, UpdateProductRequest request);
    
    /**
     * Maps list of Product entities to list of ProductResponse DTOs
     * Uses MapStruct for collection mapping
     */
    java.util.List<ProductResponse> toResponseList(java.util.List<Product> products);
}
