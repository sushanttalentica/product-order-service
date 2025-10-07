package com.ecommerce.productorder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time updates
 * 
 * Design Principles Applied:
 * - Real-Time Communication: WebSocket for bi-directional communication
 * - Publish-Subscribe Pattern: STOMP protocol for message routing
 * - Scalability: Can broadcast to thousands of connected clients
 * - Low Latency: Sub-second stock update delivery
 * 
 * Use Cases:
 * - Real-time stock updates when products are purchased
 * - Live order status notifications
 * - Inventory alerts
 * - Price change notifications
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * Configures message broker for pub-sub messaging
     * 
     * @param config the message broker registry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics
        config.enableSimpleBroker("/topic", "/queue");
        
        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // Enable user-specific destinations
        config.setUserDestinationPrefix("/user");
    }
    
    /**
     * Registers STOMP endpoints for WebSocket connections
     * 
     * @param registry the STOMP endpoint registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")  // Configure properly for production
                .withSockJS();  // Fallback for browsers without WebSocket support
        
        // Additional endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws-native")
                .setAllowedOrigins("*");
    }
}

