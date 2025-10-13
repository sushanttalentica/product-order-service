package com.ecommerce.productorder.domain.service.impl;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.domain.repository.CategoryRepository;
import com.ecommerce.productorder.domain.repository.ProductRepository;
import com.ecommerce.productorder.domain.service.ProductService;
import com.ecommerce.productorder.dto.request.CreateProductRequest;
import com.ecommerce.productorder.dto.request.UpdateProductRequest;
import com.ecommerce.productorder.dto.response.ProductResponse;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private Category testCategory;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Arrange - Setup test data
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .description("Electronic products")
                .isActive(true)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(100)
                .sku("TEST-SKU-001")
                .isActive(true)
                .category(testCategory)
                .build();

        createRequest = CreateProductRequest.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .sku("NEW-SKU-001")
                .categoryId(1L)
                .build();

        updateRequest = UpdateProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("199.99"))
                .stockQuantity(75)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse response = productService.createProduct(createRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("99.99"));
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found with ID: 1");
        
        verify(categoryRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void shouldGetProductByIdSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Optional<ProductResponse> response = productService.getProductById(1L);

        // Assert
        assertThat(response).isPresent();
        assertThat(response.get().getName()).isEqualTo("Test Product");
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when product not found")
    void shouldReturnEmptyWhenProductNotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<ProductResponse> response = productService.getProductById(1L);

        // Assert
        assertThat(response).isEmpty();
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should get product by SKU successfully")
    void shouldGetProductBySkuSuccessfully() {
        // Arrange
        when(productRepository.findBySku("TEST-SKU-001")).thenReturn(Optional.of(testProduct));

        // Act
        Optional<ProductResponse> response = productService.getProductBySku("TEST-SKU-001");

        // Assert
        assertThat(response).isPresent();
        assertThat(response.get().getSku()).isEqualTo("TEST-SKU-001");
        verify(productRepository).findBySku("TEST-SKU-001");
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void shouldGetAllProductsWithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        // Act
        Page<ProductResponse> response = productService.getAllProducts(pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("Test Product");
        verify(productRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should search products by name")
    void shouldSearchProductsByName() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findByNameContainingIgnoreCase("Test", pageable)).thenReturn(productPage);

        // Act
        Page<ProductResponse> response = productService.searchProductsByName("Test", pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(productRepository).findByNameContainingIgnoreCase("Test", pageable);
    }

    @Test
    @DisplayName("Should get products by category")
    void shouldGetProductsByCategory() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findByCategoryId(1L, pageable)).thenReturn(productPage);

        // Act
        Page<ProductResponse> response = productService.getProductsByCategory(1L, pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(productRepository).findByCategoryId(1L, pageable);
    }

    @Test
    @DisplayName("Should get products by price range")
    void shouldGetProductsByPriceRange() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findByPriceBetween(any(BigDecimal.class), any(BigDecimal.class), eq(pageable)))
                .thenReturn(productPage);

        // Act
        Page<ProductResponse> response = productService.getProductsByPriceRange(
                new BigDecimal("50.00"), new BigDecimal("200.00"), pageable);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(productRepository).findByPriceBetween(any(BigDecimal.class), any(BigDecimal.class), eq(pageable));
    }

    @Test
    @DisplayName("Should update product successfully")
    void shouldUpdateProductSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void shouldThrowExceptionWhenUpdatingNonExistentProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with ID: 1");
        
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void shouldDeleteProductSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(any(Product.class));

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository).findById(1L);
        verify(productRepository).delete(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void shouldThrowExceptionWhenDeletingNonExistentProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with ID: 1");
        
        verify(productRepository).findById(1L);
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    @DisplayName("Should get products with low stock")
    void shouldGetProductsWithLowStock() {
        // Arrange
        when(productRepository.findByStockQuantityLessThanEqual(10)).thenReturn(List.of(testProduct));

        // Act
        List<ProductResponse> response = productService.getProductsWithLowStock(10);

        // Assert
        assertThat(response).hasSize(1);
        verify(productRepository).findByStockQuantityLessThanEqual(10);
    }

    @Test
    @DisplayName("Should update product stock successfully")
    void shouldUpdateProductStockSuccessfully() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse response = productService.updateProductStock(1L, 150);

        // Assert
        assertThat(response).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when updating stock for non-existent product")
    void shouldThrowExceptionWhenUpdatingStockForNonExistentProduct() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProductStock(1L, 150))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found with ID: 1");
        
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should handle business exception for invalid price")
    void shouldHandleBusinessExceptionForInvalidPrice() {
        // Arrange
        CreateProductRequest invalidRequest = CreateProductRequest.builder()
                .name("Invalid Product")
                .price(new BigDecimal("-10.00")) // Invalid negative price
                .stockQuantity(10)
                .categoryId(1L)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Price must be greater than zero");
    }

    @Test
    @DisplayName("Should handle business exception for invalid stock")
    void shouldHandleBusinessExceptionForInvalidStock() {
        // Arrange
        CreateProductRequest invalidRequest = CreateProductRequest.builder()
                .name("Invalid Product")
                .price(new BigDecimal("10.00"))
                .stockQuantity(-5) // Invalid negative stock
                .categoryId(1L)
                .build();

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock quantity must be non-negative");
    }
}
