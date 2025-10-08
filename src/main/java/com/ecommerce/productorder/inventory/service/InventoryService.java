package com.ecommerce.productorder.inventory.service;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.Product;

import java.util.List;
import java.util.Optional;


public interface InventoryService {
    
    /**
     * Reserves inventory for order
     * Reserves product stock for order items
     * 
     * @param order the order entity
     * @return true if inventory reserved successfully, false otherwise
     */
    boolean reserveInventory(Order order);
    
    /**
     * Releases inventory for order
     * Releases reserved stock for order cancellation
     * 
     * @param order the order entity
     * @return true if inventory released successfully, false otherwise
     */
    boolean releaseInventory(Order order);
    
    /**
     * Updates inventory after order completion
     * Updates stock levels after successful order processing
     * 
     * @param order the order entity
     * @return true if inventory updated successfully, false otherwise
     */
    boolean updateInventoryAfterOrder(Order order);
    
    /**
     * Checks product availability
     * Verifies if product has sufficient stock
     * 
     * @param productId the product ID
     * @param quantity the required quantity
     * @return true if product is available, false otherwise
     */
    boolean checkProductAvailability(Long productId, Integer quantity);
    
    /**
     * Gets product stock level
     * Retrieves current stock level for product
     * 
     * @param productId the product ID
     * @return Optional containing stock level if found, empty otherwise
     */
    Optional<Integer> getProductStockLevel(Long productId);
    
    /**
     * Updates product stock level
     * Updates stock level for product
     * 
     * @param productId the product ID
     * @param newStockLevel the new stock level
     * @return true if stock updated successfully, false otherwise
     */
    boolean updateProductStockLevel(Long productId, Integer newStockLevel);
    
    /**
     * Gets low stock products
     * Retrieves products with stock below threshold
     * 
     * @param threshold the stock threshold
     * @return List of products with low stock
     */
    List<Product> getLowStockProducts(Integer threshold);
    
    /**
     * Gets out of stock products
     * Retrieves products with zero stock
     * 
     * @return List of out of stock products
     */
    List<Product> getOutOfStockProducts();
    
    /**
     * Gets inventory statistics
     * Retrieves inventory statistics and metrics
     * 
     * @return Object containing inventory statistics
     */
    Object getInventoryStatistics();
}
