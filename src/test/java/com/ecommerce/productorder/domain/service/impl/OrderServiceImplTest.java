package com.ecommerce.productorder.domain.service.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.OrderItem;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.OrderRepository;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import com.ecommerce.productorder.domain.service.OrderService;
import com.ecommerce.productorder.dto.request.CreateOrderRequest;
import com.ecommerce.productorder.dto.response.OrderResponse;
import com.ecommerce.productorder.service.OrderEventPublisher;
import com.ecommerce.productorder.exception.BusinessException;
import com.ecommerce.productorder.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl
 * 
 * Design Principles Applied:
 * - Test-Driven Development: Comprehensive test coverage
 * - AAA Pattern: Arrange, Act, Assert
 * - Mocking: Proper use of mocks for dependencies
 * - Test Isolation: Each test is independent
 * - Descriptive Test Names: Clear test method names
 * - Edge Case Testing: Tests for boundary conditions
 * - Exception Testing: Tests for error scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private Product testProduct;
    private CreateOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        // Arrange - Setup test data
        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .isActive(true)
                .build();

        testOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .customerId(1L)
                .customerEmail("test@example.com")
                .status(Order.OrderStatus.PENDING)
                .totalAmount(new BigDecimal("199.98"))
                .shippingAddress("123 Test St, Test City")
                .build();

        createRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .customerEmail("test@example.com")
                .totalAmount(new BigDecimal("199.98"))
                .shippingAddress("123 Test St, Test City")
                .build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrderSuccessfully() {
        // Arrange
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.createOrder(createRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOrderNumber()).isEqualTo("ORD-001");
        assertThat(response.getCustomerId()).isEqualTo(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderEventPublisher).publishOrderCreatedEvent(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when order items are empty")
    void shouldThrowExceptionWhenOrderItemsAreEmpty() {
        // Arrange
        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .customerEmail("test@example.com")
                .totalAmount(new BigDecimal("199.98"))
                .shippingAddress("123 Test St, Test City")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Order must contain at least one item");
    }

    @Test
    @DisplayName("Should throw exception when total amount is invalid")
    void shouldThrowExceptionWhenTotalAmountIsInvalid() {
        // Arrange
        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .customerId(1L)
                .customerEmail("test@example.com")
                .totalAmount(new BigDecimal("-10.00")) // Invalid negative amount
                .shippingAddress("123 Test St, Test City")
                .build();

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Order total amount must be greater than zero");
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void shouldGetOrderByIdSuccessfully() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        Optional<OrderResponse> response = orderService.getOrderById(1L);

        // Assert
        assertThat(response).isPresent();
        assertThat(response.get().getOrderNumber()).isEqualTo("ORD-001");
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when order not found")
    void shouldReturnEmptyWhenOrderNotFound() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<OrderResponse> response = orderService.getOrderById(1L);

        // Assert
        assertThat(response).isEmpty();
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get orders by customer ID")
    void shouldGetOrdersByCustomerId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        when(orderRepository.findByCustomerId(1L, pageable)).thenReturn(orderPage);

        // Act
        Page<OrderResponse> response = orderService.getOrdersByCustomerId(1L, pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getCustomerId()).isEqualTo(1L);
        verify(orderRepository).findByCustomerId(1L, pageable);
    }

    @Test
    @DisplayName("Should get orders by status")
    void shouldGetOrdersByStatus() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        when(orderRepository.findByStatus(Order.OrderStatus.PENDING, pageable)).thenReturn(orderPage);

        // Act
        Page<OrderResponse> response = orderService.getOrdersByStatus("PENDING", pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(orderRepository).findByStatus(Order.OrderStatus.PENDING, pageable);
    }

    @Test
    @DisplayName("Should throw exception for invalid order status")
    void shouldThrowExceptionForInvalidOrderStatus() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrdersByStatus("INVALID_STATUS", pageable))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid order status");
    }

    @Test
    @DisplayName("Should update order status successfully")
    void shouldUpdateOrderStatusSuccessfully() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.updateOrderStatus(1L, "CONFIRMED");

        // Assert
        assertThat(response).isNotNull();
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderEventPublisher).publishOrderStatusUpdatedEvent(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent order")
    void shouldThrowExceptionWhenUpdatingNonExistentOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, "CONFIRMED"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with ID: 1");
        
        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrderSuccessfully() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.cancelOrder(1L);

        // Assert
        assertThat(response).isNotNull();
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
        verify(orderEventPublisher).publishOrderCancelledEvent(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-existent order")
    void shouldThrowExceptionWhenCancellingNonExistentOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Order not found with ID: 1");
        
        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-cancellable order")
    void shouldThrowExceptionWhenCancellingNonCancellableOrder() {
        // Arrange
        Order deliveredOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .customerId(1L)
                .customerEmail("test@example.com")
                .status(Order.OrderStatus.DELIVERED)
                .totalAmount(new BigDecimal("199.98"))
                .build();
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(deliveredOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Order cannot be cancelled in current state");
        
        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should get all orders with pagination")
    void shouldGetAllOrdersWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // Act
        Page<OrderResponse> response = orderService.getAllOrders(pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(orderRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get orders by date range")
    void shouldGetOrdersByDateRange() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        when(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(orderPage);

        // Act
        Page<OrderResponse> response = orderService.getOrdersByDateRange("2023-01-01T00:00:00", "2023-12-31T23:59:59", pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(orderRepository).findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable));
    }

    @Test
    @DisplayName("Should throw exception for invalid date format")
    void shouldThrowExceptionForInvalidDateFormat() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrdersByDateRange("invalid-date", "2023-12-31T23:59:59", pageable))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid date format");
    }

    @Test
    @DisplayName("Should get orders by amount range")
    void shouldGetOrdersByAmountRange() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(List.of(testOrder), pageable, 1);
        when(orderRepository.findByTotalAmountBetween(any(BigDecimal.class), any(BigDecimal.class), eq(pageable)))
                .thenReturn(orderPage);

        // Act
        Page<OrderResponse> response = orderService.getOrdersByAmountRange("100.00", "300.00", pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(orderRepository).findByTotalAmountBetween(any(BigDecimal.class), any(BigDecimal.class), eq(pageable));
    }

    @Test
    @DisplayName("Should throw exception for invalid amount format")
    void shouldThrowExceptionForInvalidAmountFormat() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrdersByAmountRange("invalid-amount", "300.00", pageable))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid amount format");
    }

    @Test
    @DisplayName("Should get order statistics")
    void shouldGetOrderStatistics() {
        // Arrange
        when(orderRepository.count()).thenReturn(10L);
        when(orderRepository.findAll()).thenReturn(List.of(testOrder));

        // Act
        Object statistics = orderService.getOrderStatistics("2023-01-01", "2023-12-31");

        // Assert
        assertThat(statistics).isNotNull();
        verify(orderRepository).count();
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Should get orders needing attention")
    void shouldGetOrdersNeedingAttention() {
        // Arrange
        when(orderRepository.findByStatusAndCreatedAtBefore(any(Order.OrderStatus.class), any(LocalDateTime.class)))
                .thenReturn(List.of(testOrder));

        // Act
        List<OrderResponse> response = orderService.getOrdersNeedingAttention();

        // Assert
        assertThat(response).hasSize(1);
        verify(orderRepository).findByStatusAndCreatedAtBefore(any(Order.OrderStatus.class), any(LocalDateTime.class));
    }
}
