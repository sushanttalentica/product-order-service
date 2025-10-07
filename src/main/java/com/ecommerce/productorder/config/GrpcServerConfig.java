package com.ecommerce.productorder.config;

import com.ecommerce.productorder.payment.grpc.PaymentGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

/**
 * gRPC server configuration
 * 
 * Design Principles Applied:
 * - Configuration by Convention: Uses Spring Boot conventions
 * - Single Responsibility: Only handles gRPC server setup
 * - Dependency Injection: Provides gRPC server bean
 * - Lifecycle Management: Proper server startup and shutdown
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class GrpcServerConfig {
    
    @Value("${grpc.server.port:9090}")
    private int grpcPort;
    
    private final PaymentGrpcService paymentGrpcService;
    
    private Server grpcServer;
    
    /**
     * Creates and starts the gRPC server
     * 
     * @return gRPC server instance
     * @throws IOException if server fails to start
     */
    @Bean
    public Server grpcServer() throws IOException {
        log.info("Starting gRPC server on port: {}", grpcPort);
        
        grpcServer = ServerBuilder.forPort(grpcPort)
            .addService(paymentGrpcService)
            .build()
            .start();
            
        log.info("gRPC server started successfully on port: {}", grpcPort);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server...");
            if (grpcServer != null) {
                grpcServer.shutdown();
            }
        }));
        
        return grpcServer;
    }
    
    /**
     * Shutdown gRPC server on application shutdown
     */
    @PreDestroy
    public void shutdownGrpcServer() {
        if (grpcServer != null && !grpcServer.isShutdown()) {
            log.info("Shutting down gRPC server...");
            grpcServer.shutdown();
        }
    }
}
