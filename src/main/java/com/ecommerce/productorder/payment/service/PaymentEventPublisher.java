package com.ecommerce.productorder.payment.service;

import com.ecommerce.productorder.payment.domain.entity.Payment;

public interface PaymentEventPublisher {
    

    void publishPaymentProcessedEvent(Payment payment);
    

    void publishPaymentFailedEvent(Payment payment);
    

    void publishPaymentRefundedEvent(Payment payment);
    

    void publishPaymentCancelledEvent(Payment payment);
    

    void publishPaymentRetryEvent(Payment payment);
}
