package com.ecommerce.productorder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Enable simple broker for topics
    config.enableSimpleBroker("/topic", "/queue");

    // Set application destination prefix
    config.setApplicationDestinationPrefixes("/app");

    // Enable user-specific destinations
    config.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Register WebSocket endpoint
    registry
        .addEndpoint("/ws")
        .setAllowedOrigins("*")
        .withSockJS(); // Fallback for browsers without WebSocket support

    // Additional endpoint without SockJS for native WebSocket clients
    registry.addEndpoint("/ws-native").setAllowedOrigins("*");
  }
}
