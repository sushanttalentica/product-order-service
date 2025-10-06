package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Category operations
 * 
 * Design Principles Applied:
 * - RESTful Design: Follows REST conventions for HTTP methods and status codes
 * - Single Responsibility: Only handles Category HTTP operations
 * - Dependency Injection: Uses constructor injection for dependencies
 * - Security: Public access for category listing
 * - Logging: Uses SLF4J for logging
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    
    private final CategoryRepository categoryRepository;
    
    /**
     * Retrieves all categories
     * 
     * Design Principles Applied:
     * - GET for retrieval: Follows REST conventions
     * - Security: Public access for category listing
     * - List Response: Returns list of all categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        log.debug("Retrieving all categories");
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Retrieves a category by ID
     * 
     * Design Principles Applied:
     * - GET for retrieval: Follows REST conventions
     * - Security: Public access for category details
     * - Path Variable: Uses path variable for resource identification
     * - Optional Response: Handles case when category not found
     */
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long categoryId) {
        log.debug("Retrieving category with id: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
