package com.ecommerce.productorder.util;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.Product;
import com.ecommerce.productorder.dto.request.CreateOrderRequest;
import com.ecommerce.productorder.dto.request.CreateProductRequest;
import com.ecommerce.productorder.dto.request.UpdateProductRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class TestDataFactory {

    private static final Random random = new Random();
    private static final String[] PRODUCT_NAMES = {
        "Laptop", "Smartphone", "Tablet", "Headphones", "Camera",
        "Watch", "Speaker", "Mouse", "Keyboard", "Monitor"
    };
    private static final String[] CATEGORY_NAMES = {
        "Electronics", "Clothing", "Books", "Home & Garden", "Sports"
    };
    private static final String[] CUSTOMER_EMAILS = {
        "john.doe@example.com", "jane.smith@example.com", "bob.wilson@example.com",
        "alice.brown@example.com", "charlie.davis@example.com"
    };

    /**
     * Creates a test category
     * 
     * @return Category entity
     */
    public static Category createCategory() {
        return Category.builder()
                .name(CATEGORY_NAMES[random.nextInt(CATEGORY_NAMES.length)])
                .description("Test category description")
                .isActive(true)
                .build();
    }

    /**
     * Creates a test category with specific name
     * 
     * @param name the category name
     * @return Category entity
     */
    public static Category createCategory(String name) {
        return Category.builder()
                .name(name)
                .description("Test category description for " + name)
                .isActive(true)
                .build();
    }

    /**
     * Creates a test product
     * 
     * @param category the category for the product
     * @return Product entity
     */
    public static Product createProduct(Category category) {
        return Product.builder()
                .name(PRODUCT_NAMES[random.nextInt(PRODUCT_NAMES.length)])
                .description("Test product description")
                .price(new BigDecimal("99.99").add(new BigDecimal(random.nextInt(1000))))
                .stockQuantity(100 + random.nextInt(900))
                .sku("TEST-SKU-" + random.nextInt(10000))
                .isActive(true)
                .category(category)
                .build();
    }

    /**
     * Creates a test product with specific name
     * 
     * @param name the product name
     * @param category the category for the product
     * @return Product entity
     */
    public static Product createProduct(String name, Category category) {
        return Product.builder()
                .name(name)
                .description("Test product description for " + name)
                .price(new BigDecimal("99.99").add(new BigDecimal(random.nextInt(1000))))
                .stockQuantity(100 + random.nextInt(900))
                .sku("TEST-SKU-" + name.replaceAll("\\s+", "-").toUpperCase() + "-" + random.nextInt(10000))
                .isActive(true)
                .category(category)
                .build();
    }

    /**
     * Creates a test order
     * 
     * @return Order entity
     */
    public static Order createOrder() {
        return Order.builder()
                .orderNumber("ORD-" + random.nextInt(100000))
                .customerId((long) (1 + random.nextInt(100)))
                .customerEmail(CUSTOMER_EMAILS[random.nextInt(CUSTOMER_EMAILS.length)])
                .status(Order.OrderStatus.PENDING)
                .totalAmount(new BigDecimal("199.98").add(new BigDecimal(random.nextInt(1000))))
                .shippingAddress("123 Test St, Test City, TC 12345")
                .build();
    }

    /**
     * Creates a test order with specific customer ID
     * 
     * @param customerId the customer ID
     * @return Order entity
     */
    public static Order createOrder(Long customerId) {
        return Order.builder()
                .orderNumber("ORD-" + random.nextInt(100000))
                .customerId(customerId)
                .customerEmail(CUSTOMER_EMAILS[random.nextInt(CUSTOMER_EMAILS.length)])
                .status(Order.OrderStatus.PENDING)
                .totalAmount(new BigDecimal("199.98").add(new BigDecimal(random.nextInt(1000))))
                .shippingAddress("123 Test St, Test City, TC 12345")
                .build();
    }

    /**
     * Creates a test order with specific status
     * 
     * @param status the order status
     * @return Order entity
     */
    public static Order createOrder(Order.OrderStatus status) {
        return Order.builder()
                .orderNumber("ORD-" + random.nextInt(100000))
                .customerId((long) (1 + random.nextInt(100)))
                .customerEmail(CUSTOMER_EMAILS[random.nextInt(CUSTOMER_EMAILS.length)])
                .status(status)
                .totalAmount(new BigDecimal("199.98").add(new BigDecimal(random.nextInt(1000))))
                .shippingAddress("123 Test St, Test City, TC 12345")
                .build();
    }

    /**
     * Creates a CreateProductRequest
     * 
     * @param categoryId the category ID
     * @return CreateProductRequest
     */
    public static CreateProductRequest createProductRequest(Long categoryId) {
        return CreateProductRequest.builder()
                .name(PRODUCT_NAMES[random.nextInt(PRODUCT_NAMES.length)])
                .description("Test product description")
                .price(new BigDecimal("99.99").add(new BigDecimal(random.nextInt(1000))))
                .stockQuantity(100 + random.nextInt(900))
                .sku("TEST-SKU-" + random.nextInt(10000))
                .categoryId(categoryId)
                .build();
    }

    /**
     * Creates a CreateProductRequest with specific name
     * 
     * @param name the product name
     * @param categoryId the category ID
     * @return CreateProductRequest
     */
    public static CreateProductRequest createProductRequest(String name, Long categoryId) {
        return CreateProductRequest.builder()
                .name(name)
                .description("Test product description for " + name)
                .price(new BigDecimal("99.99").add(new BigDecimal(random.nextInt(1000))))
                .stockQuantity(100 + random.nextInt(900))
                .sku("TEST-SKU-" + name.replaceAll("\\s+", "-").toUpperCase() + "-" + random.nextInt(10000))
                .categoryId(categoryId)
                .build();
    }

    /**
     * Creates an UpdateProductRequest
     * 
     * @return UpdateProductRequest
     */
    public static UpdateProductRequest createUpdateProductRequest() {
        return UpdateProductRequest.builder()
                .name("Updated " + PRODUCT_NAMES[random.nextInt(PRODUCT_NAMES.length)])
                .description("Updated test product description")
                .price(new BigDecimal("199.99").add(new BigDecimal(random.nextInt(1000))))
                .stockQuantity(200 + random.nextInt(800))
                .build();
    }

    /**
     * Creates a CreateOrderRequest
     * 
     * @return CreateOrderRequest
     */
    public static CreateOrderRequest createOrderRequest() {
        return CreateOrderRequest.builder()
                .customerId((long) (1 + random.nextInt(100)))
                .customerEmail(CUSTOMER_EMAILS[random.nextInt(CUSTOMER_EMAILS.length)])
                .totalAmount(new BigDecimal("199.98").add(new BigDecimal(random.nextInt(1000))))
                .shippingAddress("123 Test St, Test City, TC 12345")
                .build();
    }

    /**
     * Creates a CreateOrderRequest with specific customer ID
     * 
     * @param customerId the customer ID
     * @return CreateOrderRequest
     */
    public static CreateOrderRequest createOrderRequest(Long customerId) {
        return CreateOrderRequest.builder()
                .customerId(customerId)
                .customerEmail(CUSTOMER_EMAILS[random.nextInt(CUSTOMER_EMAILS.length)])
                .totalAmount(new BigDecimal("199.98").add(new BigDecimal(random.nextInt(1000))))
                .shippingAddress("123 Test St, Test City, TC 12345")
                .build();
    }

    /**
     * Creates a list of test categories
     * 
     * @param count the number of categories to create
     * @return List of Category entities
     */
    public static List<Category> createCategories(int count) {
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            categories.add(createCategory());
        }
        return categories;
    }

    /**
     * Creates a list of test products
     * 
     * @param count the number of products to create
     * @param category the category for the products
     * @return List of Product entities
     */
    public static List<Product> createProducts(int count, Category category) {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            products.add(createProduct(category));
        }
        return products;
    }

    /**
     * Creates a list of test orders
     * 
     * @param count the number of orders to create
     * @return List of Order entities
     */
    public static List<Order> createOrders(int count) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            orders.add(createOrder());
        }
        return orders;
    }

    /**
     * Creates a list of test orders with specific customer ID
     * 
     * @param count the number of orders to create
     * @param customerId the customer ID
     * @return List of Order entities
     */
    public static List<Order> createOrders(int count, Long customerId) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            orders.add(createOrder(customerId));
        }
        return orders;
    }

    /**
     * Creates a list of test orders with specific status
     * 
     * @param count the number of orders to create
     * @param status the order status
     * @return List of Order entities
     */
    public static List<Order> createOrders(int count, Order.OrderStatus status) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            orders.add(createOrder(status));
        }
        return orders;
    }

    /**
     * Creates a list of CreateProductRequest objects
     * 
     * @param count the number of requests to create
     * @param categoryId the category ID
     * @return List of CreateProductRequest objects
     */
    public static List<CreateProductRequest> createProductRequests(int count, Long categoryId) {
        List<CreateProductRequest> requests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            requests.add(createProductRequest(categoryId));
        }
        return requests;
    }

    /**
     * Creates a list of CreateOrderRequest objects
     * 
     * @param count the number of requests to create
     * @return List of CreateOrderRequest objects
     */
    public static List<CreateOrderRequest> createOrderRequests(int count) {
        List<CreateOrderRequest> requests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            requests.add(createOrderRequest());
        }
        return requests;
    }

    /**
     * Creates a list of CreateOrderRequest objects with specific customer ID
     * 
     * @param count the number of requests to create
     * @param customerId the customer ID
     * @return List of CreateOrderRequest objects
     */
    public static List<CreateOrderRequest> createOrderRequests(int count, Long customerId) {
        List<CreateOrderRequest> requests = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            requests.add(createOrderRequest(customerId));
        }
        return requests;
    }

    /**
     * Creates a random order status
     * 
     * @return Order.OrderStatus
     */
    public static Order.OrderStatus randomOrderStatus() {
        Order.OrderStatus[] statuses = Order.OrderStatus.values();
        return statuses[random.nextInt(statuses.length)];
    }

    /**
     * Creates a random product name
     * 
     * @return String
     */
    public static String randomProductName() {
        return PRODUCT_NAMES[random.nextInt(PRODUCT_NAMES.length)];
    }

    /**
     * Creates a random category name
     * 
     * @return String
     */
    public static String randomCategoryName() {
        return CATEGORY_NAMES[random.nextInt(CATEGORY_NAMES.length)];
    }

    /**
     * Creates a random customer email
     * 
     * @return String
     */
    public static String randomCustomerEmail() {
        return CUSTOMER_EMAILS[random.nextInt(CUSTOMER_EMAILS.length)];
    }

    /**
     * Creates a random price
     * 
     * @return BigDecimal
     */
    public static BigDecimal randomPrice() {
        return new BigDecimal("99.99").add(new BigDecimal(random.nextInt(1000)));
    }

    /**
     * Creates a random stock quantity
     * 
     * @return Integer
     */
    public static Integer randomStockQuantity() {
        return 100 + random.nextInt(900);
    }

    /**
     * Creates a random SKU
     * 
     * @return String
     */
    public static String randomSku() {
        return "TEST-SKU-" + random.nextInt(10000);
    }

    /**
     * Creates a random order number
     * 
     * @return String
     */
    public static String randomOrderNumber() {
        return "ORD-" + random.nextInt(100000);
    }

    /**
     * Creates a random customer ID
     * 
     * @return Long
     */
    public static Long randomCustomerId() {
        return (long) (1 + random.nextInt(100));
    }
}
