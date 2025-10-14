package com.ecommerce.productorder.domain.repository;

import com.ecommerce.productorder.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameAndIsActiveTrue(String name);

    List<Category> findByIsActiveTrue();

    boolean existsByNameAndIsActiveTrue(String name);

    @Query("SELECT c FROM Category c LEFT JOIN c.products p " +
           "WHERE c.isActive = true " +
           "GROUP BY c.id, c.name, c.description, c.isActive, c.createdAt, c.updatedAt " +
           "HAVING COUNT(p) > 0")
    List<Category> findCategoriesWithProducts();

    Optional<Category> findByNameIgnoreCaseAndIsActiveTrue(String name);
}
