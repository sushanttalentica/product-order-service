package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.api.CategoriesApi;
import com.ecommerce.productorder.api.model.*;
import com.ecommerce.productorder.domain.entity.Category;
import com.ecommerce.productorder.domain.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class CategoriesApiImpl implements CategoriesApi {

    private final CategoryRepository categoryRepository;    
    public CategoriesApiImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ResponseEntity<List<CategoryResponseApi>> getAllCategories() {
        log.info("Getting all categories");
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories.stream()
                .map(this::convertToApiModel)
                .collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<CategoryResponseApi> getCategoryById(Long categoryId) {
        log.info("Getting category by ID: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .map(category -> ResponseEntity.ok(convertToApiModel(category)))
                .orElse(ResponseEntity.status(404).body(null));
    }

    private CategoryResponseApi convertToApiModel(Category entity) {
        var apiModel = new CategoryResponseApi();
        apiModel.setId(entity.getId());
        apiModel.setName(entity.getName());
        apiModel.setDescription(entity.getDescription());
        apiModel.setIsActive(entity.getIsActive());
        return apiModel;
    }
}
