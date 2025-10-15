package com.ecommerce.productorder.mapper;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.dto.request.CreateOrderRequest;
import com.ecommerce.productorder.dto.response.OrderResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequest request) {
        if (request == null) {
            return null;
        }

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(Order.OrderStatus.PENDING);
        return order;
    }

    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderResponse.OrderItemResponse> orderItemResponses = null;
        if (order.getOrderItems() != null) {
            orderItemResponses = order.getOrderItems().stream()
                    .map(item -> new OrderResponse.OrderItemResponse(
                            item.getId(),
                            new OrderResponse.OrderItemResponse.ProductInfo(
                                    item.getProduct().getId(),
                                    item.getProduct().getName(),
                                    item.getProduct().getSku()
                            ),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getSubtotal()
                    ))
                    .collect(Collectors.toList());
        }

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getCustomerEmail(),
                order.getStatus() != null ? order.getStatus().name() : null,
                order.getTotalAmount(),
                order.getShippingAddress(),
                orderItemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public List<OrderResponse> toResponseList(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
