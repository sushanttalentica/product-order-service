package com.ecommerce.productorder.domain.service;

import com.ecommerce.productorder.dto.request.CreateOrderRequest;
import com.ecommerce.productorder.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    Optional<OrderResponse> getOrderById(Long orderId);

    Page<OrderResponse> getOrdersByCustomerId(Long customerId, Pageable pageable);

    Page<OrderResponse> getOrdersByStatus(String status, Pageable pageable);

    OrderResponse updateOrderStatus(Long orderId, String status);

    OrderResponse cancelOrder(Long orderId);

    Page<OrderResponse> getAllOrders(Pageable pageable);

    Page<OrderResponse> getOrdersByDateRange(String startDate, String endDate, Pageable pageable);

    Page<OrderResponse> getOrdersByAmountRange(String minAmount, String maxAmount, Pageable pageable);

    Object getOrderStatistics(String startDate, String endDate);

    List<OrderResponse> getOrdersNeedingAttention();
}