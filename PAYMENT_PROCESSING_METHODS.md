# Payment Processing Methods - Complete Flow

## Overview

Payment processing in your Product Order Service follows a **layered architecture** with multiple methods handling different aspects of the payment flow. Here's the complete breakdown:

## 1. Entry Points for Payment Processing

### **Primary Entry Point: gRPC Service**
```java
// File: PaymentGrpcService.java
@Override
public void processPayment(ProcessPaymentRequest request, StreamObserver<ProcessPaymentResponse> responseObserver) {
    log.info("Processing payment via gRPC for order ID: {}", request.getOrderId());
    
    try {
        // Convert gRPC request to internal DTO
        ProcessPaymentRequest internalRequest = convertToInternalRequest(request);
        
        // Process payment through service
        PaymentResponse paymentResponse = paymentService.processPayment(internalRequest);
        
        // Convert response to gRPC format
        ProcessPaymentResponse grpcResponse = ProcessPaymentResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Payment processed successfully")
                .setPayment(convertToGrpcPaymentData(paymentResponse))
                .build();
        
        responseObserver.onNext(grpcResponse);
        responseObserver.onCompleted();
        
    } catch (Exception e) {
        // Handle errors
        ProcessPaymentResponse errorResponse = ProcessPaymentResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Payment processing failed: " + e.getMessage())
                .build();
        
        responseObserver.onNext(errorResponse);
        responseObserver.onCompleted();
    }
}
```

**This is the MAIN ENTRY POINT** for payment processing via gRPC.

## 2. Core Business Logic Method

### **PaymentService.processPayment() - Main Business Logic**
```java
// File: PaymentServiceImpl.java
@Override
@Transactional
public PaymentResponse processPayment(ProcessPaymentRequest request) {
    log.info("Processing payment for order ID: {}, amount: {}", request.getOrderId(), request.getAmount());
    
    try {
        // 1. Validate request
        validatePaymentRequest(request);
        
        // 2. Check for existing payment
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());
        if (existingPayment.isPresent()) {
            throw new BusinessException("Payment already exists for order ID: " + request.getOrderId());
        }
        
        // 3. Create payment entity
        Payment payment = createPaymentEntity(request);
        
        // 4. Save payment
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created with ID: {}", savedPayment.getPaymentId());
        
        // 5. Process payment through gateway
        PaymentResponse gatewayResponse = processPaymentThroughGateway(savedPayment, request);
        
        // 6. Update payment status based on gateway response
        updatePaymentStatus(savedPayment, gatewayResponse);
        
        // 7. Publish payment event
        paymentEventPublisher.publishPaymentProcessedEvent(savedPayment);
        
        log.info("Payment processed successfully for order ID: {}", request.getOrderId());
        return PaymentResponse.fromEntity(savedPayment);
        
    } catch (Exception e) {
        log.error("Error processing payment for order ID: {}", request.getOrderId(), e);
        throw new BusinessException("Failed to process payment: " + e.getMessage());
    }
}
```

**This is the CORE BUSINESS LOGIC** method that orchestrates the entire payment process.

## 3. Gateway Communication Method

### **PaymentGatewayService.processPayment() - External Gateway Communication**
```java
// File: PaymentGatewayServiceImpl.java
@Override
public PaymentResponse processPayment(Payment payment, ProcessPaymentRequest request) {
    log.info("Processing payment through gateway for payment ID: {}", payment.getPaymentId());
    
    try {
        // 1. Validate payment method
        if (!validatePaymentMethod(request)) {
            return createFailedResponse(payment, "Invalid payment method or card details");
        }
        
        // 2. Simulate gateway processing delay
        simulateProcessingDelay();
        
        // 3. Simulate payment processing result
        boolean isSuccessful = simulatePaymentResult(request);
        
        if (isSuccessful) {
            return createSuccessfulResponse(payment, request);
        } else {
            return createFailedResponse(payment, "Payment authorization failed");
        }
        
    } catch (Exception e) {
        log.error("Error processing payment through gateway for payment ID: {}", payment.getPaymentId(), e);
        return createFailedResponse(payment, "Gateway communication error: " + e.getMessage());
    }
}
```

**This method handles communication with external payment gateways.**

## 4. Complete Payment Processing Flow

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PAYMENT PROCESSING FLOW                          │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   gRPC      │───▶│   Payment   │───▶│   Payment    │───▶│   Payment   │
│   Client    │    │   Service   │    │   Gateway   │    │   Entity    │
│  Request    │    │  (Business) │    │  Service    │    │  (Database)│
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │                   │
        │                   │                   │                   │
        ▼                   ▼                   ▼                   ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   gRPC      │    │   Payment   │    │   Gateway   │    │   Payment   │
│  Response   │    │   Events    │    │  Response   │    │  Status     │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## 5. Method Call Chain

### **Step-by-Step Method Calls:**

1. **gRPC Entry Point:**
   ```java
   PaymentGrpcService.processPayment(ProcessPaymentRequest, StreamObserver)
   ```

2. **Business Logic:**
   ```java
   PaymentServiceImpl.processPayment(ProcessPaymentRequest)
   ```

3. **Gateway Communication:**
   ```java
   PaymentGatewayServiceImpl.processPayment(Payment, ProcessPaymentRequest)
   ```

4. **Database Operations:**
   ```java
   PaymentRepository.save(Payment)
   PaymentRepository.findByOrderId(Long)
   ```

5. **Event Publishing:**
   ```java
   PaymentEventPublisher.publishPaymentProcessedEvent(Payment)
   ```

## 6. Key Methods and Their Responsibilities

### **1. PaymentGrpcService.processPayment()**
- **Purpose**: gRPC entry point for payment processing
- **Responsibilities**:
  - Convert gRPC request to internal DTO
  - Call business logic service
  - Convert response to gRPC format
  - Handle gRPC communication
- **File**: `PaymentGrpcService.java`

### **2. PaymentServiceImpl.processPayment()**
- **Purpose**: Core business logic for payment processing
- **Responsibilities**:
  - Validate payment request
  - Check for existing payments
  - Create payment entity
  - Save payment to database
  - Call gateway service
  - Update payment status
  - Publish payment events
- **File**: `PaymentServiceImpl.java`

### **3. PaymentGatewayServiceImpl.processPayment()**
- **Purpose**: External gateway communication
- **Responsibilities**:
  - Validate payment method
  - Simulate gateway processing
  - Handle gateway responses
  - Create success/failure responses
- **File**: `PaymentGatewayServiceImpl.java`

### **4. Payment.processPayment()**
- **Purpose**: Entity-level payment processing
- **Responsibilities**:
  - Update payment status
  - Set transaction ID
  - Set gateway response
- **File**: `Payment.java`

## 7. Method Execution Order

```
1. PaymentGrpcService.processPayment()           // gRPC Entry Point
   ↓
2. PaymentServiceImpl.processPayment()            // Business Logic
   ↓
3. PaymentGatewayServiceImpl.processPayment()     // Gateway Communication
   ↓
4. Payment.processPayment()                      // Entity Update
   ↓
5. PaymentEventPublisher.publishPaymentProcessedEvent()  // Event Publishing
```

## 8. Error Handling Methods

### **Error Handling in Each Layer:**

#### **gRPC Layer:**
```java
try {
    // Process payment
} catch (Exception e) {
    ProcessPaymentResponse errorResponse = ProcessPaymentResponse.newBuilder()
            .setSuccess(false)
            .setMessage("Payment processing failed: " + e.getMessage())
            .build();
    
    responseObserver.onNext(errorResponse);
    responseObserver.onCompleted();
}
```

#### **Business Logic Layer:**
```java
try {
    // Business logic
} catch (Exception e) {
    log.error("Error processing payment for order ID: {}", request.getOrderId(), e);
    throw new BusinessException("Failed to process payment: " + e.getMessage());
}
```

#### **Gateway Layer:**
```java
try {
    // Gateway communication
} catch (Exception e) {
    log.error("Error processing payment through gateway for payment ID: {}", payment.getPaymentId(), e);
    return createFailedResponse(payment, "Gateway communication error: " + e.getMessage());
}
```

## 9. Transaction Management

### **@Transactional Annotations:**
```java
@Service
@Transactional  // Class-level transaction
public class PaymentServiceImpl implements PaymentService {
    
    @Override
    @Transactional  // Method-level transaction
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        // Transactional business logic
    }
}
```

## 10. Summary

### **Main Payment Processing Methods:**

1. **`PaymentGrpcService.processPayment()`** - **gRPC Entry Point**
2. **`PaymentServiceImpl.processPayment()`** - **Core Business Logic**
3. **`PaymentGatewayServiceImpl.processPayment()`** - **Gateway Communication**

### **Supporting Methods:**

4. **`Payment.processPayment()`** - **Entity Update**
5. **`PaymentEventPublisher.publishPaymentProcessedEvent()`** - **Event Publishing**
6. **`PaymentRepository.save()`** - **Database Operations**

### **Key Points:**

- **gRPC Service** is the main entry point for external requests
- **Payment Service** contains the core business logic
- **Gateway Service** handles external payment gateway communication
- **Entity methods** handle data updates
- **Event Publisher** handles asynchronous event processing
- **Repository** handles database operations

The payment processing follows a **layered architecture** where each method has a specific responsibility, ensuring separation of concerns and maintainability! 🚀
