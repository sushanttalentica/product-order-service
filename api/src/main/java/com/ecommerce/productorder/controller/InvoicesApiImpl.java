package com.ecommerce.productorder.controller;

import com.ecommerce.productorder.api.InvoicesApi;
import com.ecommerce.productorder.api.model.CheckInvoiceExists200Response;
import com.ecommerce.productorder.api.model.InvoiceUrlResponse;
import com.ecommerce.productorder.api.model.MessageResponse;
import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.domain.repository.OrderRepository;
import com.ecommerce.productorder.invoice.service.InvoiceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class InvoicesApiImpl implements InvoicesApi {

    private final InvoiceService invoiceService;
    private final OrderRepository orderRepository;    
    public InvoicesApiImpl(InvoiceService invoiceService, OrderRepository orderRepository) {
        this.invoiceService = invoiceService;
        this.orderRepository = orderRepository;
    }

    @Override
    public ResponseEntity<InvoiceUrlResponse> generateInvoice(Long orderId) {
        log.info("Generating invoice for order: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        return invoiceService.generateInvoice(order)
                .map(url -> ResponseEntity.status(201).body(new InvoiceUrlResponse().url(url)))
                .orElse(ResponseEntity.status(500).body(null));
    }

    @Override
    public ResponseEntity<InvoiceUrlResponse> getInvoiceUrl(Long orderId) {
        log.info("Getting invoice URL for order: {}", orderId);
        return invoiceService.getInvoiceUrl(orderId)
                .map(url -> ResponseEntity.ok(new InvoiceUrlResponse().url(url)))
                .orElse(ResponseEntity.status(404).body(null));
    }

    @Override
    public ResponseEntity<MessageResponse> deleteInvoice(Long orderId) {
        log.info("Deleting invoice for order: {}", orderId);
        invoiceService.deleteInvoice(orderId);
        return ResponseEntity.ok(new MessageResponse()
                .message("Invoice deleted successfully")
                .success(true));
    }

    @Override
    public ResponseEntity<CheckInvoiceExists200Response> checkInvoiceExists(Long orderId) {
        boolean exists = invoiceService.invoiceExists(orderId);
        var response = new CheckInvoiceExists200Response();
        response.setExists(exists);
        return ResponseEntity.ok(response);
    }
}
