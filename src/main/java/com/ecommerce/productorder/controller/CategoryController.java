package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.repository.CategoryRepository;
import com.ecommerce.productorder.dto.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    
    private final CategoryRepository categoryRepository;
    
    
    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        log.debug("Retrieving all categories");
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }
    
    
    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long categoryId) {
        log.debug("Retrieving category with id: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .map(category -> ResponseEntity.ok((Object) category))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(MessageResponse.error("Category not found with ID: " + categoryId)));
    }
}
