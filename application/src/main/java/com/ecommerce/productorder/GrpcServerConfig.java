package com.ecommerce.productorder;

import com.ecommerce.productorder.config.GrpcProperties;
import com.ecommerce.productorder.payment.grpc.PaymentGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class GrpcServerConfig {

  private final GrpcProperties grpcProperties;
  private final PaymentGrpcService paymentGrpcService;

  public GrpcServerConfig(GrpcProperties grpcProperties, PaymentGrpcService paymentGrpcService) {
    this.grpcProperties = grpcProperties;
    this.paymentGrpcService = paymentGrpcService;
  }

  private Server grpcServer;

  @Bean
  public Server grpcServer() throws IOException {
    int port = grpcProperties.getPort();
    log.info("Starting gRPC server on port: {}", port);

    grpcServer = ServerBuilder.forPort(port).addService(paymentGrpcService).build().start();

    log.info("gRPC server started successfully on port: {}", port);

    // Add shutdown hook
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("Shutting down gRPC server...");
                  if (grpcServer != null) {
                    grpcServer.shutdown();
                  }
                }));

    return grpcServer;
  }

  // Graceful shutdown
  @PreDestroy
  public void shutdownGrpcServer() {
    if (grpcServer != null && !grpcServer.isShutdown()) {
      log.info("Shutting down gRPC server...");
      grpcServer.shutdown();
    }
  }
}
