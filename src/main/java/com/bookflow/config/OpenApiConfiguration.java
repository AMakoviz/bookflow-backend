package com.bookflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

  @Bean
  OpenAPI bookFlowOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("BookFlow API")
                .version("v1")
                .description("Conflict-safe meeting-room booking API"));
  }
}
