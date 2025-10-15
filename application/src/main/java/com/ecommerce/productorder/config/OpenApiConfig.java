package com.ecommerce.productorder.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private final OpenApiProperties openApiProperties;

  public OpenApiConfig(OpenApiProperties openApiProperties) {
    this.openApiProperties = openApiProperties;
  }

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title(openApiProperties.getInfo().getTitle())
                .version(openApiProperties.getInfo().getVersion())
                .description(openApiProperties.getInfo().getDescription()))
        .servers(
            openApiProperties.getServers().stream()
                .map(s -> new Server().url(s.getUrl()).description(s.getDescription()))
                .collect(Collectors.toList()))
        .addSecurityItem(
            new SecurityRequirement().addList(openApiProperties.getSecurity().getSchemeName()))
        .components(
            new Components()
                .addSecuritySchemes(
                    openApiProperties.getSecurity().getSchemeName(),
                    new SecurityScheme()
                        .name(openApiProperties.getSecurity().getSchemeName())
                        .type(
                            SecurityScheme.Type.valueOf(
                                openApiProperties.getSecurity().getSchemeType()))
                        .scheme(openApiProperties.getSecurity().getScheme())
                        .bearerFormat(openApiProperties.getSecurity().getBearerFormat())));
  }
}
