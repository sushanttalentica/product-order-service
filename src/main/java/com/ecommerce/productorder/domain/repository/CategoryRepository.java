package com.ecommerce.productorder.domain.repository;

import com.ecommerce.productorder.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find category by name
     * Uses Spring Data JPA query derivation
     */
    Optional<Category> findByNameAndIsActiveTrue(String name);
    
    /**
     * Find all active categories
     * Uses Spring Data JPA query derivation
     */
    List<Category> findByIsActiveTrue();
    
    /**
     * Check if category exists by name
     * Uses Spring Data JPA query derivation
     */
    boolean existsByNameAndIsActiveTrue(String name);
    
    /**
     * Find categories with products count
     * Uses custom JPQL query for complex business logic
     */
    @Query("SELECT c FROM Category c LEFT JOIN c.products p " +
           "WHERE c.isActive = true " +
           "GROUP BY c.id, c.name, c.description, c.isActive, c.createdAt, c.updatedAt " +
           "HAVING COUNT(p) > 0")
    List<Category> findCategoriesWithProducts();
    
    /**
     * Find category by name (case-insensitive)
     * Uses Spring Data JPA query derivation
     */
    Optional<Category> findByNameIgnoreCaseAndIsActiveTrue(String name);
}
