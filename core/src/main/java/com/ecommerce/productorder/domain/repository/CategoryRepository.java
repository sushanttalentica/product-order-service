package com.ecommerce.productorder.domain.repository;

import com.ecommerce.productorder.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameAndActiveTrue(String name);

    List<Category> findByActiveTrue();

    boolean existsByNameAndActiveTrue(String name);

    @Query("SELECT c FROM Category c LEFT JOIN c.products p " +
           "WHERE c.active = true " +
           "GROUP BY c.id, c.name, c.description, c.active, c.createdAt, c.updatedAt " +
           "HAVING COUNT(p) > 0")
    List<Category> findCategoriesWithProducts();

    Optional<Category> findByNameIgnoreCaseAndActiveTrue(String name);
}
