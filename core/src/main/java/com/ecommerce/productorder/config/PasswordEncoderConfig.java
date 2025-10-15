package com.ecommerce.productorder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

// Password Encoder Configuration
@Configuration
public class PasswordEncoderConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    // For demo purposes, using NoOpPasswordEncoder
    // In production, will use BCryptPasswordEncoder
    return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
  }
}
