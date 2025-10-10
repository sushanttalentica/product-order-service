package com.ecommerce.productorder.inventory.service;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.entity.Product;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    

    boolean reserveInventory(Order order);
    

    boolean releaseInventory(Order order);
    

    boolean updateInventoryAfterOrder(Order order);
    

    boolean checkProductAvailability(Long productId, Integer quantity);
    

    Optional<Integer> getProductStockLevel(Long productId);
    

    boolean updateProductStockLevel(Long productId, Integer newStockLevel);
    

    List<Product> getLowStockProducts(Integer threshold);
    

    List<Product> getOutOfStockProducts();
    

    Object getInventoryStatistics();
}
