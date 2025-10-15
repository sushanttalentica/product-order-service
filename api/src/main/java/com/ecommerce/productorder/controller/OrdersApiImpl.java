package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.api.OrdersApi;
import com.ecommerce.productorder.api.model.*;
import com.ecommerce.productorder.domain.service.OrderService;
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
public class OrdersApiImpl implements OrdersApi {

    private final OrderService orderService;    
    public OrdersApiImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public ResponseEntity<OrderResponse> createOrder(CreateOrderRequest createOrderRequest) {
        log.info("Creating order for customer: {}", createOrderRequest.getCustomerId());
        
        com.ecommerce.productorder.dto.request.CreateOrderRequest dtoRequest = 
                new com.ecommerce.productorder.dto.request.CreateOrderRequest();
        dtoRequest.setCustomerId(createOrderRequest.getCustomerId());
        dtoRequest.setCustomerEmail(createOrderRequest.getCustomerEmail());
        dtoRequest.setShippingAddress(createOrderRequest.getShippingAddress());
        dtoRequest.setOrderItems(createOrderRequest.getOrderItems().stream()
                .map(item -> {
                    com.ecommerce.productorder.dto.request.CreateOrderRequest.OrderItemRequest orderItem = 
                            new com.ecommerce.productorder.dto.request.CreateOrderRequest.OrderItemRequest();
                    orderItem.setProductId(item.getProductId());
                    orderItem.setQuantity(item.getQuantity());
                    return orderItem;
                })
                .collect(Collectors.toList()));
        
        var response = orderService.createOrder(dtoRequest);
        return ResponseEntity.status(201).body(convertToApiModel(response));
    }

    @Override
    public ResponseEntity<Object> getAllOrders(Integer page, Integer size) {
        Page<com.ecommerce.productorder.dto.response.OrderResponse> ordersPage = 
                orderService.getAllOrders(PageRequest.of(page, size));
        return ResponseEntity.ok(ordersPage);
    }

    @Override
    public ResponseEntity<OrderResponse> getOrderById(Long orderId) {
        return orderService.getOrderById(orderId)
                .map(this::convertToApiModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(null));
    }

    @Override
    public ResponseEntity<MessageResponse> cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(new MessageResponse()
                .message("Order cancelled successfully")
                .success(true));
    }

    @Override
    public ResponseEntity<List<com.ecommerce.productorder.api.model.OrderResponse>> getOrdersByCustomer(Long customerId) {
        var orders = orderService.getOrdersByCustomerId(customerId, PageRequest.of(0, 100));
        return ResponseEntity.ok(orders.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<List<com.ecommerce.productorder.api.model.OrderResponse>> getOrdersByStatus(String status) {
        var orders = orderService.getOrdersByStatus(status, PageRequest.of(0, 100));
        return ResponseEntity.ok(orders.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<OrderResponse> updateOrderStatus(Long orderId, String status) {
        log.info("Updating order {} status to: {}", orderId, status);
        var response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(convertToApiModel(response));
    }

    @Override
    public ResponseEntity<List<com.ecommerce.productorder.api.model.OrderResponse>> getOrdersByDateRange(
            OffsetDateTime startDate, OffsetDateTime endDate) {
        var orders = orderService.getOrdersByDateRange(
                startDate.toString(), 
                endDate.toString(), 
                PageRequest.of(0, 100));
        return ResponseEntity.ok(orders.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<List<com.ecommerce.productorder.api.model.OrderResponse>> getOrdersByAmountRange(
            BigDecimal minAmount, BigDecimal maxAmount) {
        var orders = orderService.getOrdersByAmountRange(
                minAmount.toString(), 
                maxAmount.toString(), 
                PageRequest.of(0, 100));
        return ResponseEntity.ok(orders.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<OrderStatistics> getOrderStatistics() {
        var stats = (java.util.Map<String, Object>) orderService.getOrderStatistics(null, null);
        var apiStats = new OrderStatistics();
        apiStats.setTotalOrders(stats.get("totalOrders") != null ? ((Number) stats.get("totalOrders")).longValue() : 0L);
        apiStats.setTotalRevenue(stats.get("totalRevenue") != null ? (BigDecimal) stats.get("totalRevenue") : BigDecimal.ZERO);
        apiStats.setAverageOrderValue(stats.get("averageOrderValue") != null ? (BigDecimal) stats.get("averageOrderValue") : BigDecimal.ZERO);
        return ResponseEntity.ok(apiStats);
    }

    @Override
    public ResponseEntity<List<com.ecommerce.productorder.api.model.OrderResponse>> getOrdersNeedingAttention() {
        var orders = orderService.getOrdersNeedingAttention();
        return ResponseEntity.ok(orders.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    private com.ecommerce.productorder.api.model.OrderResponse convertToApiModel(
            com.ecommerce.productorder.dto.response.OrderResponse dto) {
        var apiModel = new com.ecommerce.productorder.api.model.OrderResponse();
        apiModel.setId(dto.id());
        apiModel.setOrderNumber(dto.orderNumber());
        apiModel.setCustomerId(dto.customerId());
        apiModel.setCustomerEmail(dto.customerEmail());
        apiModel.setStatus(dto.status());
        apiModel.setTotalAmount(dto.totalAmount());
        apiModel.setShippingAddress(dto.shippingAddress());
        apiModel.setCreatedAt(dto.createdAt() != null ? dto.createdAt().atOffset(ZoneOffset.UTC) : null);
        apiModel.setUpdatedAt(dto.updatedAt() != null ? dto.updatedAt().atOffset(ZoneOffset.UTC) : null);
        
        if (dto.orderItems() != null) {
            apiModel.setOrderItems(dto.orderItems().stream()
                    .map(item -> {
                        var apiItem = new OrderItemResponse();
                        apiItem.setId(item.id());
                        apiItem.setQuantity(item.quantity());
                        apiItem.setUnitPrice(item.unitPrice());
                        apiItem.setSubtotal(item.subtotal());
                        
                        var productInfo = new ProductInfo();
                        productInfo.setId(item.product().id());
                        productInfo.setName(item.product().name());
                        productInfo.setSku(item.product().sku());
                        apiItem.setProduct(productInfo);
                        
                        return apiItem;
                    })
                    .collect(Collectors.toList()));
        }
        
        return apiModel;
    }
}

