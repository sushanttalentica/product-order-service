package com.ecommerce.productorder.service;

import com.ecommerce.productorder.domain.entity.Order;


public interface OrderEventPublisher {
    
    /**
     * Publishes order created event
     * Notifies other services about new order creation
     * 
     * @param order the order entity
     */
    void publishOrderCreatedEvent(Order order);
    
    /**
     * Publishes order status updated event
     * Notifies other services about order status changes
     * 
     * @param order the order entity
     */
    void publishOrderStatusUpdatedEvent(Order order);
    
    /**
     * Publishes order cancelled event
     * Notifies other services about order cancellation
     * 
     * @param order the order entity
     */
    void publishOrderCancelledEvent(Order order);
    
    /**
     * Publishes order completed event
     * Notifies other services about order completion
     * 
     * @param order the order entity
     */
    void publishOrderCompletedEvent(Order order);
}
