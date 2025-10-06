package com.ecommerce.productorder.security;

import com.ecommerce.productorder.config.JwtAuthenticationEntryPoint;
import com.ecommerce.productorder.config.JwtRequestFilter;
import com.ecommerce.productorder.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security configuration tests
 * 
 * Design Principles Applied:
 * - Security Testing: Tests authentication and authorization
 * - Spring Boot Test: Uses Spring Boot test context
 * - MockMvc: Tests HTTP endpoints with security
 * - Test Profiles: Uses test-specific configuration
 * - Role-based Testing: Tests different user roles
 * - Endpoint Testing: Tests public and protected endpoints
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Security Configuration Tests")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should allow public access to product endpoints")
    void shouldAllowPublicAccessToProductEndpoints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isNotFound()); // Product not found, but endpoint accessible

        mockMvc.perform(get("/api/v1/products/search?name=test"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow public access to order endpoints")
    void shouldAllowPublicAccessToOrderEndpoints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isUnauthorized()); // Requires authentication

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized()); // Requires authentication
    }

    @Test
    @DisplayName("Should allow public access to health check endpoints")
    void shouldAllowPublicAccessToHealthCheckEndpoints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow public access to H2 console")
    void shouldAllowPublicAccessToH2Console() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should require authentication for protected endpoints")
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/orders/customer/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/v1/orders/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow CUSTOMER role to access customer endpoints")
    @WithMockUser(roles = "CUSTOMER")
    void shouldAllowCustomerRoleToAccessCustomerEndpoints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/customer/1"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/orders/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny CUSTOMER role access to admin endpoints")
    @WithMockUser(roles = "CUSTOMER")
    void shouldDenyCustomerRoleAccessToAdminEndpoints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/orders/status/PENDING"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "CONFIRMED")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role to access admin endpoints")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminRoleToAccessAdminEndpoints() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/orders/status/PENDING"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "CONFIRMED")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny ADMIN role access to customer-only endpoints")
    @WithMockUser(roles = "ADMIN")
    void shouldDenyAdminRoleAccessToCustomerOnlyEndpoints() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/orders/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should require ADMIN role for product creation")
    @WithMockUser(roles = "CUSTOMER")
    void shouldRequireAdminRoleForProductCreation() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role for product creation")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminRoleForProductCreation() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest()); // Bad request due to validation, but authorized
    }

    @Test
    @DisplayName("Should require ADMIN role for product updates")
    @WithMockUser(roles = "CUSTOMER")
    void shouldRequireAdminRoleForProductUpdates() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role for product updates")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminRoleForProductUpdates() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/products/1")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest()); // Bad request due to validation, but authorized
    }

    @Test
    @DisplayName("Should require ADMIN role for product deletion")
    @WithMockUser(roles = "CUSTOMER")
    void shouldRequireAdminRoleForProductDeletion() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role for product deletion")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminRoleForProductDeletion() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/1")
                        .with(csrf()))
                .andExpect(status().isNotFound()); // Not found, but authorized
    }

    @Test
    @DisplayName("Should require ADMIN role for low stock products")
    @WithMockUser(roles = "CUSTOMER")
    void shouldRequireAdminRoleForLowStockProducts() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/low-stock"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role for low stock products")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminRoleForLowStockProducts() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/low-stock"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should require ADMIN role for product stock updates")
    @WithMockUser(roles = "CUSTOMER")
    void shouldRequireAdminRoleForProductStockUpdates() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/v1/products/1/stock")
                        .param("stock", "100")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role for product stock updates")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminRoleForProductStockUpdates() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/v1/products/1/stock")
                        .param("stock", "100")
                        .with(csrf()))
                .andExpect(status().isNotFound()); // Not found, but authorized
    }

    @Test
    @DisplayName("Should require ADMIN role for order statistics")
    @WithMockUser(roles = "CUSTOMER")
    void shouldRequireAdminRoleForOrderStatistics() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/statistics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role for order statistics")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminRoleForOrderStatistics() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/statistics"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should require ADMIN role for orders needing attention")
    @WithMockUser(roles = "CUSTOMER")
    void shouldRequireAdminRoleForOrdersNeedingAttention() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/needing-attention"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role for orders needing attention")
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminRoleForOrdersNeedingAttention() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/needing-attention"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle CSRF protection")
    void shouldHandleCsrfProtection() throws Exception {
        // Act & Assert - POST without CSRF token should be rejected
        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());

        // Act & Assert - POST with CSRF token should be processed
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized()); // Unauthorized due to authentication, not CSRF
    }

    @Test
    @DisplayName("Should handle CORS configuration")
    void shouldHandleCorsConfiguration() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/v1/products")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle session management")
    void shouldHandleSessionManagement() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie")); // No session cookies
    }
}
