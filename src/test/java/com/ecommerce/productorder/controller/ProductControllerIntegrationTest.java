package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.CategoryRepository;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import com.ecommerce.productorder.dto.request.CreateProductRequest;
import com.ecommerce.productorder.dto.request.UpdateProductRequest;
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
@DisplayName("Product Controller Integration Tests")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Clean up database
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
    }

    @Test
    @DisplayName("Should create product successfully with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProductSuccessfullyWithAdminRole() throws Exception {
        // Arrange
        CreateProductRequest request = CreateProductRequest.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .sku("NEW-SKU-001")
                .categoryId(testCategory.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.stockQuantity").value(50))
                .andExpect(jsonPath("$.sku").value("NEW-SKU-001"));
    }

    @Test
    @DisplayName("Should return forbidden when creating product without ADMIN role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWhenCreatingProductWithoutAdminRole() throws Exception {
        // Arrange
        CreateProductRequest request = CreateProductRequest.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .sku("NEW-SKU-001")
                .categoryId(testCategory.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return unauthorized when creating product without authentication")
    void shouldReturnUnauthorizedWhenCreatingProductWithoutAuthentication() throws Exception {
        // Arrange
        CreateProductRequest request = CreateProductRequest.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .sku("NEW-SKU-001")
                .categoryId(testCategory.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void shouldGetProductByIdSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.stockQuantity").value(100))
                .andExpect(jsonPath("$.sku").value("TEST-SKU-001"));
    }

    @Test
    @DisplayName("Should return not found for non-existent product")
    void shouldReturnNotFoundForNonExistentProduct() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get product by SKU successfully")
    void shouldGetProductBySkuSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/sku/{sku}", testProduct.getSku()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("TEST-SKU-001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    @DisplayName("Should return not found for non-existent SKU")
    void shouldReturnNotFoundForNonExistentSku() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/sku/{sku}", "NON-EXISTENT-SKU"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void shouldGetAllProductsWithPagination() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("Should search products by name")
    void shouldSearchProductsByName() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search")
                        .param("name", "Test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("Should get products by category")
    void shouldGetProductsByCategory() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/category/{categoryId}", testCategory.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("Should get products by price range")
    void shouldGetProductsByPriceRange() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/price-range")
                        .param("minPrice", "50.00")
                        .param("maxPrice", "200.00")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }

    @Test
    @DisplayName("Should update product successfully with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateProductSuccessfullyWithAdminRole() throws Exception {
        // Arrange
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("199.99"))
                .stockQuantity(75)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/{id}", testProduct.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.stockQuantity").value(75));
    }

    @Test
    @DisplayName("Should return forbidden when updating product without ADMIN role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWhenUpdatingProductWithoutAdminRole() throws Exception {
        // Arrange
        UpdateProductRequest request = UpdateProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("199.99"))
                .stockQuantity(75)
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/products/{id}", testProduct.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should delete product successfully with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteProductSuccessfullyWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/{id}", testProduct.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return forbidden when deleting product without ADMIN role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWhenDeletingProductWithoutAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/products/{id}", testProduct.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should get products with low stock with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldGetProductsWithLowStockWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return forbidden when getting low stock products without ADMIN role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWhenGettingLowStockProductsWithoutAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should update product stock with ADMIN role")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateProductStockWithAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/v1/products/{id}/stock", testProduct.getId())
                        .param("stock", "150")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(150));
    }

    @Test
    @DisplayName("Should return forbidden when updating product stock without ADMIN role")
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnForbiddenWhenUpdatingProductStockWithoutAdminRole() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/v1/products/{id}/stock", testProduct.getId())
                        .param("stock", "150")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should validate request body for invalid data")
    @WithMockUser(roles = "ADMIN")
    void shouldValidateRequestBodyForInvalidData() throws Exception {
        // Arrange
        CreateProductRequest invalidRequest = CreateProductRequest.builder()
                .name("") // Invalid empty name
                .description("Test Description")
                .price(new BigDecimal("-10.00")) // Invalid negative price
                .stockQuantity(-5) // Invalid negative stock
                .sku("") // Invalid empty SKU
                .categoryId(testCategory.getId())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }

    @Test
    @DisplayName("Should handle advanced search with multiple criteria")
    void shouldHandleAdvancedSearchWithMultipleCriteria() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/products/search/advanced")
                        .param("name", "Test")
                        .param("categoryId", testCategory.getId().toString())
                        .param("minPrice", "50.00")
                        .param("maxPrice", "200.00")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Test Product"));
    }
}
