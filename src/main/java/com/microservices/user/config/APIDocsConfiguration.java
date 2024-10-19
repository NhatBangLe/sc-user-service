package com.microservices.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class APIDocsConfiguration {

    @Value("${API_DOCS_SERVER}")
    private String API_DOCS_SERVER;

    @Bean
    public OpenAPI getOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service")
                        .version("1.0.0"))
                .servers(List.of(new Server().url(API_DOCS_SERVER)));
    }

}
