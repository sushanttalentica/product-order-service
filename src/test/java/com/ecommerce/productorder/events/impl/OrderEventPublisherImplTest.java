package com.ecommerce.productorder.events.impl;

import com.ecommerce.productorder.domain.entity.Order;
import com.ecommerce.productorder.events.OrderEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Order Event Publisher Tests")
class OrderEventPublisherImplTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderEventPublisherImpl orderEventPublisher;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Arrange - Setup test data
        testOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .customerId(1L)
                .customerEmail("test@example.com")
                .status(Order.OrderStatus.PENDING)
                .totalAmount(new BigDecimal("199.98"))
                .shippingAddress("123 Test St, Test City")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should publish order created event successfully")
    void shouldPublishOrderCreatedEventSuccessfully() {
        // Act
        orderEventPublisher.publishOrderCreatedEvent(testOrder);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), dataCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("order.created");
        assertThat(keyCaptor.getValue()).isEqualTo("1");
        
        Map<String, Object> eventData = dataCaptor.getValue();
        assertThat(eventData.get("eventType")).isEqualTo("ORDER_CREATED");
        assertThat(eventData.get("orderId")).isEqualTo(1L);
        assertThat(eventData.get("orderNumber")).isEqualTo("ORD-001");
        assertThat(eventData.get("customerId")).isEqualTo(1L);
        assertThat(eventData.get("customerEmail")).isEqualTo("test@example.com");
        assertThat(eventData.get("status")).isEqualTo("PENDING");
        assertThat(eventData.get("totalAmount")).isEqualTo(new BigDecimal("199.98"));
        assertThat(eventData.get("serviceName")).isEqualTo("product-order-service");
        assertThat(eventData.get("serviceVersion")).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Should publish order status updated event successfully")
    void shouldPublishOrderStatusUpdatedEventSuccessfully() {
        // Act
        orderEventPublisher.publishOrderStatusUpdatedEvent(testOrder);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), dataCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("order.status.updated");
        assertThat(keyCaptor.getValue()).isEqualTo("1");
        
        Map<String, Object> eventData = dataCaptor.getValue();
        assertThat(eventData.get("eventType")).isEqualTo("ORDER_STATUS_UPDATED");
        assertThat(eventData.get("orderId")).isEqualTo(1L);
        assertThat(eventData.get("previousStatus")).isEqualTo("PENDING");
        assertThat(eventData.get("serviceName")).isEqualTo("product-order-service");
    }

    @Test
    @DisplayName("Should publish order cancelled event successfully")
    void shouldPublishOrderCancelledEventSuccessfully() {
        // Act
        orderEventPublisher.publishOrderCancelledEvent(testOrder);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), dataCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("order.cancelled");
        assertThat(keyCaptor.getValue()).isEqualTo("1");
        
        Map<String, Object> eventData = dataCaptor.getValue();
        assertThat(eventData.get("eventType")).isEqualTo("ORDER_CANCELLED");
        assertThat(eventData.get("orderId")).isEqualTo(1L);
        assertThat(eventData.get("serviceName")).isEqualTo("product-order-service");
    }

    @Test
    @DisplayName("Should publish order completed event successfully")
    void shouldPublishOrderCompletedEventSuccessfully() {
        // Act
        orderEventPublisher.publishOrderCompletedEvent(testOrder);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), dataCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("order.completed");
        assertThat(keyCaptor.getValue()).isEqualTo("1");
        
        Map<String, Object> eventData = dataCaptor.getValue();
        assertThat(eventData.get("eventType")).isEqualTo("ORDER_COMPLETED");
        assertThat(eventData.get("orderId")).isEqualTo(1L);
        assertThat(eventData.get("serviceName")).isEqualTo("product-order-service");
    }

    @Test
    @DisplayName("Should handle Kafka exception gracefully for order created event")
    void shouldHandleKafkaExceptionGracefullyForOrderCreatedEvent() {
        // Arrange
        doThrow(new RuntimeException("Kafka connection failed"))
                .when(kafkaTemplate).send(anyString(), anyString(), any(Map.class));

        // Act & Assert - Should not throw exception
        assertThatCode(() -> orderEventPublisher.publishOrderCreatedEvent(testOrder))
                .doesNotThrowAnyException();

        verify(kafkaTemplate).send(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Should handle Kafka exception gracefully for order status updated event")
    void shouldHandleKafkaExceptionGracefullyForOrderStatusUpdatedEvent() {
        // Arrange
        doThrow(new RuntimeException("Kafka connection failed"))
                .when(kafkaTemplate).send(anyString(), anyString(), any(Map.class));

        // Act & Assert - Should not throw exception
        assertThatCode(() -> orderEventPublisher.publishOrderStatusUpdatedEvent(testOrder))
                .doesNotThrowAnyException();

        verify(kafkaTemplate).send(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Should handle Kafka exception gracefully for order cancelled event")
    void shouldHandleKafkaExceptionGracefullyForOrderCancelledEvent() {
        // Arrange
        doThrow(new RuntimeException("Kafka connection failed"))
                .when(kafkaTemplate).send(anyString(), anyString(), any(Map.class));

        // Act & Assert - Should not throw exception
        assertThatCode(() -> orderEventPublisher.publishOrderCancelledEvent(testOrder))
                .doesNotThrowAnyException();

        verify(kafkaTemplate).send(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Should handle Kafka exception gracefully for order completed event")
    void shouldHandleKafkaExceptionGracefullyForOrderCompletedEvent() {
        // Arrange
        doThrow(new RuntimeException("Kafka connection failed"))
                .when(kafkaTemplate).send(anyString(), anyString(), any(Map.class));

        // Act & Assert - Should not throw exception
        assertThatCode(() -> orderEventPublisher.publishOrderCompletedEvent(testOrder))
                .doesNotThrowAnyException();

        verify(kafkaTemplate).send(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("Should include all required fields in order created event")
    void shouldIncludeAllRequiredFieldsInOrderCreatedEvent() {
        // Act
        orderEventPublisher.publishOrderCreatedEvent(testOrder);

        // Assert
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaTemplate).send(anyString(), anyString(), dataCaptor.capture());

        Map<String, Object> eventData = dataCaptor.getValue();
        
        // Required fields
        assertThat(eventData).containsKey("eventType");
        assertThat(eventData).containsKey("eventId");
        assertThat(eventData).containsKey("timestamp");
        assertThat(eventData).containsKey("orderId");
        assertThat(eventData).containsKey("orderNumber");
        assertThat(eventData).containsKey("customerId");
        assertThat(eventData).containsKey("customerEmail");
        assertThat(eventData).containsKey("status");
        assertThat(eventData).containsKey("totalAmount");
        assertThat(eventData).containsKey("shippingAddress");
        assertThat(eventData).containsKey("createdAt");
        assertThat(eventData).containsKey("updatedAt");
        assertThat(eventData).containsKey("serviceName");
        assertThat(eventData).containsKey("serviceVersion");
    }

    @Test
    @DisplayName("Should include all required fields in order status updated event")
    void shouldIncludeAllRequiredFieldsInOrderStatusUpdatedEvent() {
        // Act
        orderEventPublisher.publishOrderStatusUpdatedEvent(testOrder);

        // Assert
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaTemplate).send(anyString(), anyString(), dataCaptor.capture());

        Map<String, Object> eventData = dataCaptor.getValue();
        
        // Required fields
        assertThat(eventData).containsKey("eventType");
        assertThat(eventData).containsKey("eventId");
        assertThat(eventData).containsKey("timestamp");
        assertThat(eventData).containsKey("orderId");
        assertThat(eventData).containsKey("orderNumber");
        assertThat(eventData).containsKey("customerId");
        assertThat(eventData).containsKey("customerEmail");
        assertThat(eventData).containsKey("status");
        assertThat(eventData).containsKey("totalAmount");
        assertThat(eventData).containsKey("shippingAddress");
        assertThat(eventData).containsKey("createdAt");
        assertThat(eventData).containsKey("updatedAt");
        assertThat(eventData).containsKey("previousStatus");
        assertThat(eventData).containsKey("serviceName");
        assertThat(eventData).containsKey("serviceVersion");
    }

    @Test
    @DisplayName("Should include all required fields in order cancelled event")
    void shouldIncludeAllRequiredFieldsInOrderCancelledEvent() {
        // Act
        orderEventPublisher.publishOrderCancelledEvent(testOrder);

        // Assert
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaTemplate).send(anyString(), anyString(), dataCaptor.capture());

        Map<String, Object> eventData = dataCaptor.getValue();
        
        // Required fields
        assertThat(eventData).containsKey("eventType");
        assertThat(eventData).containsKey("eventId");
        assertThat(eventData).containsKey("timestamp");
        assertThat(eventData).containsKey("orderId");
        assertThat(eventData).containsKey("orderNumber");
        assertThat(eventData).containsKey("customerId");
        assertThat(eventData).containsKey("customerEmail");
        assertThat(eventData).containsKey("status");
        assertThat(eventData).containsKey("totalAmount");
        assertThat(eventData).containsKey("shippingAddress");
        assertThat(eventData).containsKey("createdAt");
        assertThat(eventData).containsKey("updatedAt");
        assertThat(eventData).containsKey("serviceName");
        assertThat(eventData).containsKey("serviceVersion");
    }

    @Test
    @DisplayName("Should include all required fields in order completed event")
    void shouldIncludeAllRequiredFieldsInOrderCompletedEvent() {
        // Act
        orderEventPublisher.publishOrderCompletedEvent(testOrder);

        // Assert
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaTemplate).send(anyString(), anyString(), dataCaptor.capture());

        Map<String, Object> eventData = dataCaptor.getValue();
        
        // Required fields
        assertThat(eventData).containsKey("eventType");
        assertThat(eventData).containsKey("eventId");
        assertThat(eventData).containsKey("timestamp");
        assertThat(eventData).containsKey("orderId");
        assertThat(eventData).containsKey("orderNumber");
        assertThat(eventData).containsKey("customerId");
        assertThat(eventData).containsKey("customerEmail");
        assertThat(eventData).containsKey("status");
        assertThat(eventData).containsKey("totalAmount");
        assertThat(eventData).containsKey("shippingAddress");
        assertThat(eventData).containsKey("createdAt");
        assertThat(eventData).containsKey("updatedAt");
        assertThat(eventData).containsKey("serviceName");
        assertThat(eventData).containsKey("serviceVersion");
    }

    @Test
    @DisplayName("Should generate unique event IDs for each event")
    void shouldGenerateUniqueEventIdsForEachEvent() {
        // Act
        orderEventPublisher.publishOrderCreatedEvent(testOrder);
        orderEventPublisher.publishOrderStatusUpdatedEvent(testOrder);
        orderEventPublisher.publishOrderCancelledEvent(testOrder);
        orderEventPublisher.publishOrderCompletedEvent(testOrder);

        // Assert
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaTemplate, times(4)).send(anyString(), anyString(), dataCaptor.capture());

        List<Map<String, Object>> allEventData = dataCaptor.getAllValues();
        List<String> eventIds = allEventData.stream()
                .map(eventData -> (String) eventData.get("eventId"))
                .toList();

        assertThat(eventIds).hasSize(4);
        assertThat(eventIds).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Should include timestamp in all events")
    void shouldIncludeTimestampInAllEvents() {
        // Act
        orderEventPublisher.publishOrderCreatedEvent(testOrder);

        // Assert
        ArgumentCaptor<Map<String, Object>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(kafkaTemplate).send(anyString(), anyString(), dataCaptor.capture());

        Map<String, Object> eventData = dataCaptor.getValue();
        assertThat(eventData.get("timestamp")).isNotNull();
        assertThat(eventData.get("timestamp")).isInstanceOf(String.class);
    }
}
