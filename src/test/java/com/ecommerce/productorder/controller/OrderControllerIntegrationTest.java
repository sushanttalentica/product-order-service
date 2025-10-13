package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.CategoryRepository;
import com.ecommerce.productorder.domain.repository.OrderRepository;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import com.ecommerce.productorder.dto.request.CreateOrderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Order Controller Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Category testCategory;
    private Product testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Clean up database
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test category
        testCategory = Category.builder()
                .name("Electronics")
                .description("Electronic products")
                .isActive(true)
                .build();
        testCategory = categoryRepository.save(testCategory);

        // Create test product
        testProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .sku("TEST-SKU-001")
                .isActive(true)
                .category(testCategory)
                .build();
        testProduct = productRepository.save(testProduct);

        // Create test order
        testOrder = Order.builder()
                .orderNumber("ORD-001")
                .customerId(1L)
                .customerEmail("test@example.com")
                .status(Order.OrderStatus.PENDING)
                .totalAmount(new BigDecimal("199.98"))
                .shippingAddress("123 Test St, Test City")
                .build();
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("Should create order successfully with CUSTOMER role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldCreateOrderSuccessfullyWithCustomerRole() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .customerEmail("customer@example.com")
                .totalAmount(new BigDecimal("199.98"))
                .shippingAddress("123 Customer St, Customer City")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerEmail").value("customer@example.com"))
                .andExpect(jsonPath("$.totalAmount").value(199.98))
                .andExpect(jsonPath("$.shippingAddress").value("123 Customer St, Customer City"));
    }

    @Test
    @DisplayName("Should return forbidden when creating order without CUSTOMER role")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnForbiddenWhenCreatingOrderWithoutCustomerRole() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .customerEmail("customer@example.com")
                .totalAmount(new BigDecimal("199.98"))
                .shippingAddress("123 Customer St, Customer City")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return unauthorized when creating order without authentication")
    void shouldReturnUnauthorizedWhenCreatingOrderWithoutAuthentication() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(1L)
                .customerEmail("customer@example.com")
                .totalAmount(new BigDecimal("199.98"))
                .shippingAddress("123 Customer St, Customer City")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get order by ID successfully with CUSTOMER role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldGetOrderByIdSuccessfullyWithCustomerRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/{id}", testOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerEmail").value("test@example.com"))
                .andExpect(jsonPath("$.totalAmount").value(199.98));
    }

    @Test
    @DisplayName("Should return not found for non-existent order")
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnNotFoundForNonExistentOrder() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get orders by customer ID with CUSTOMER role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldGetOrdersByCustomerIdWithCustomerRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/customer/{customerId}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].customerId").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should get orders by status with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldGetOrdersByStatusWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/status/{status}", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("Should return forbidden when getting orders by status without ADMIN role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWhenGettingOrdersByStatusWithoutAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/status/{status}", "PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should update order status with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateOrderStatusWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/{id}/status", testOrder.getId())
                        .param("status", "CONFIRMED")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Should return forbidden when updating order status without ADMIN role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWhenUpdatingOrderStatusWithoutAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/{id}/status", testOrder.getId())
                        .param("status", "CONFIRMED")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should cancel order with CUSTOMER role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldCancelOrderWithCustomerRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/orders/{id}", testOrder.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Should return forbidden when cancelling order without CUSTOMER role")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnForbiddenWhenCancellingOrderWithoutCustomerRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/orders/{id}", testOrder.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get all orders with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllOrdersWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should return forbidden when getting all orders without ADMIN role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWhenGettingAllOrdersWithoutAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get orders by date range with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldGetOrdersByDateRangeWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/date-range")
                        .param("startDate", "2023-01-01T00:00:00")
                        .param("endDate", "2023-12-31T23:59:59")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("Should get orders by amount range with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldGetOrdersByAmountRangeWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/amount-range")
                        .param("minAmount", "100.00")
                        .param("maxAmount", "300.00")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    @DisplayName("Should get order statistics with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldGetOrderStatisticsWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/statistics")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2023-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    @Test
    @DisplayName("Should get orders needing attention with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldGetOrdersNeedingAttentionWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/needing-attention"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should validate request body for invalid data")
    @WithMockUser(roles = "CUSTOMER")
    void shouldValidateRequestBodyForInvalidData() throws Exception {
        // Arrange
        CreateOrderRequest invalidRequest = CreateOrderRequest.builder()
                .customerId(null) // Invalid null customer ID
                .customerEmail("") // Invalid empty email
                .totalAmount(new BigDecimal("-10.00")) // Invalid negative amount
                .shippingAddress("") // Invalid empty address
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should handle invalid order status")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleInvalidOrderStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/v1/orders/{id}/status", testOrder.getId())
                        .param("status", "INVALID_STATUS")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle invalid date format")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleInvalidDateFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/date-range")
                        .param("startDate", "invalid-date")
                        .param("endDate", "2023-12-31T23:59:59")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle invalid amount format")
    @WithMockUser(roles = "ADMIN")
    void shouldHandleInvalidAmountFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/amount-range")
                        .param("minAmount", "invalid-amount")
                        .param("maxAmount", "300.00")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
}
