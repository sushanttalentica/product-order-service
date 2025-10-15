package com.ecommerce.productorder.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openapi")
@Getter
@Setter
public class OpenApiProperties {

  private Info info = new Info();
  private List<ServerConfig> servers;
  private Security security = new Security();

  @Getter
  @Setter
  public static class Info {
    private String title;
    private String version;
    private String description;
  }

  @Getter
  @Setter
  public static class ServerConfig {
    private String url;
    private String description;
  }

  @Getter
  @Setter
  public static class Security {
    private String schemeName;
    private String schemeType;
    private String scheme;
    private String bearerFormat;
  }
}
