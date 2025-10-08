package com.ecommerce.productorder.payment.service;

import com.ecommerce.productorder.payment.domain.entity.Payment;


public interface PaymentEventPublisher {
    
    /**
     * Publishes payment processed event
     * Notifies other services about successful payment processing
     * 
     * @param payment the payment entity
     */
    void publishPaymentProcessedEvent(Payment payment);
    
    /**
     * Publishes payment failed event
     * Notifies other services about failed payment processing
     * 
     * @param payment the payment entity
     */
    void publishPaymentFailedEvent(Payment payment);
    
    /**
     * Publishes payment refunded event
     * Notifies other services about payment refund
     * 
     * @param payment the payment entity
     */
    void publishPaymentRefundedEvent(Payment payment);
    
    /**
     * Publishes payment cancelled event
     * Notifies other services about payment cancellation
     * 
     * @param payment the payment entity
     */
    void publishPaymentCancelledEvent(Payment payment);
    
    /**
     * Publishes payment retry event
     * Notifies other services about payment retry
     * 
     * @param payment the payment entity
     */
    void publishPaymentRetryEvent(Payment payment);
}
