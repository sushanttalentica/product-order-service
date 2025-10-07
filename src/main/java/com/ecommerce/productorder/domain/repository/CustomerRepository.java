package com.ecommerce.productorder.domain.repository;

import com.ecommerce.productorder.domain.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Customer Repository
 * 
 * Repository interface for Customer entity operations.
 * 
 * Design Principles Applied:
 * - Repository Pattern: Encapsulates data access logic
 * - Spring Data JPA: Provides automatic query generation
 * - Custom Queries: Defines specific business queries
 * - Pagination Support: Uses Pageable for large datasets
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by username
     */
    Optional<Customer> findByUsername(String username);

    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Find customer by username or email
     */
    Optional<Customer> findByUsernameOrEmail(String username, String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find active customers
     */
    List<Customer> findByIsActiveTrue();

    /**
     * Find customers by role
     */
    List<Customer> findByRole(Customer.CustomerRole role);

    /**
     * Find active customers by role
     */
    List<Customer> findByRoleAndIsActiveTrue(Customer.CustomerRole role);

    /**
     * Find customers by first name containing (case insensitive)
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> findByFirstNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find customers by last name containing (case insensitive)
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> findByLastNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find customers by full name containing (case insensitive)
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> findByFullNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Find customers by city
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.address.city) = LOWER(:city)")
    List<Customer> findByCity(@Param("city") String city);

    /**
     * Find customers by country
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.address.country) = LOWER(:country)")
    List<Customer> findByCountry(@Param("country") String country);

    /**
     * Find customers with verified emails
     */
    List<Customer> findByEmailVerifiedTrue();

    /**
     * Find customers with unverified emails
     */
    List<Customer> findByEmailVerifiedFalse();

    /**
     * Count customers by role
     */
    long countByRole(Customer.CustomerRole role);

    /**
     * Count active customers
     */
    long countByIsActiveTrue();

    /**
     * Find customers created after date
     */
    List<Customer> findByCreatedAtAfter(java.time.LocalDateTime date);

    /**
     * Find customers created before date
     */
    List<Customer> findByCreatedAtBefore(java.time.LocalDateTime date);

    /**
     * Find customers by date range
     */
    List<Customer> findByCreatedAtBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    /**
     * Search customers by multiple criteria
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(:name IS NULL OR LOWER(CONCAT(c.firstName, ' ', c.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:role IS NULL OR c.role = :role) AND " +
           "(:isActive IS NULL OR c.isActive = :isActive)")
    Page<Customer> searchCustomers(
            @Param("name") String name,
            @Param("email") String email,
            @Param("role") Customer.CustomerRole role,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    /**
     * Find top customers by order count
     */
    @Query("SELECT c FROM Customer c LEFT JOIN c.orders o WHERE c.isActive = true " +
           "GROUP BY c.id ORDER BY COUNT(o.id) DESC")
    Page<Customer> findTopCustomersByOrderCount(Pageable pageable);

    /**
     * Find customers with no orders
     */
    @Query("SELECT c FROM Customer c LEFT JOIN c.orders o WHERE c.isActive = true AND o.id IS NULL")
    List<Customer> findCustomersWithNoOrders();
}
