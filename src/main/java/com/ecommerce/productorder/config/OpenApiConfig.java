package com.ecommerce.productorder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger Configuration
 * 
 * This configuration sets up:
 * - API documentation with proper metadata
 * - JWT authentication documentation
 * - Server information
 * - Contact and license information
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Order Service API")
                        .description("""
                            A comprehensive e-commerce microservice for managing products, orders, and payments.
                            
                            ## Features:
                            - Product Management (CRUD operations)
                            - Order Processing
                            - Payment Integration
                            - Category Management
                            - JWT Authentication
                            - Redis Caching
                            - Kafka Event Publishing
                            - gRPC Payment Service
                            
                            ## Authentication:
                            Use JWT Bearer tokens for protected endpoints. Login via `/api/v1/auth/login` to get a token.
                            """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Product Order Service Team")
                                .email("support@productorder.com")
                                .url("https://productorder.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/product-order-service")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.productorder.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from login endpoint")));
    }
}
