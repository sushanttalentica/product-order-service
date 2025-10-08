package com.ecommerce.productorder.mapper;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.dto.request.CreateOrderRequest;
import com.ecommerce.productorder.dto.response.OrderResponse;
import org.mapstruct.*;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {
    
    /**
     * Maps CreateOrderRequest to Order entity
     * Uses MapStruct for automatic mapping with custom logic
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(CreateOrderRequest request);
    
    /**
     * Maps Order entity to OrderResponse DTO
     * Uses MapStruct for automatic mapping
     */
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToString")
    OrderResponse toResponse(Order order);
    
    /**
     * Maps list of Order entities to list of OrderResponse DTOs
     * Uses MapStruct for collection mapping
     */
    java.util.List<OrderResponse> toResponseList(java.util.List<Order> orders);
    
    /**
     * Custom mapping method for OrderStatus enum to String
     * Encapsulates enum-to-string conversion logic
     */
    @Named("statusToString")
    default String statusToString(Order.OrderStatus status) {
        return status != null ? status.name() : null;
    }
}
