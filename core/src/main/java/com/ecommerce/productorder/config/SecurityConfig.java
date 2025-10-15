package com.ecommerce.productorder.config;

import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtRequestFilter jwtRequestFilter;
  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  public SecurityConfig(
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      JwtRequestFilter jwtRequestFilter,
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) {
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    this.jwtRequestFilter = jwtRequestFilter;
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
  }

  // Authentication Provider Bean
  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    authProvider.setHideUserNotFoundExceptions(false); // Don't hide user not found exceptions
    return authProvider;
  }

  // Authentication Manager Bean
  @Bean
  public AuthenticationManager authenticationManager() throws Exception {
    return new org.springframework.security.authentication.ProviderManager(
        authenticationProvider());
  }

  // Security Filter Chain
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            authz ->
                authz
                    // Public endpoints - OpenAPI/Swagger (MUST be first)
                    .requestMatchers(new AntPathRequestMatcher("/error"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/v3/api-docs"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api-docs/**"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/swagger-resources/**"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/webjars/**"))
                    .permitAll()
                    // Public endpoints - Application
                    .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/**"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/actuator/**"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/h2-console/**"))
                    .permitAll()
                    // Products & Categories - Public read access
                    .requestMatchers(new AntPathRequestMatcher("/api/v1/products", "GET"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/v1/products/**", "GET"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/api/v1/categories/**", "GET"))
                    .permitAll()
                    // API endpoints - Authentication required, RBAC at method level via @PreAuthorize
                    .requestMatchers(new AntPathRequestMatcher("/api/v1/**"))
                    .authenticated()
                    // Protected endpoints
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider())
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

    // H2 Console configuration
    http.headers(headers -> headers.frameOptions().disable());

    return http.build();
  }

  // CORS Configuration
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
