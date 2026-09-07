package com.ordering.auth.config; // change per file: .order.config / .auth.config

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${api.base-url:http://localhost:8080}")
    private String apiBaseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(schemeName,
                        new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .servers(java.util.List.of(new Server().url(apiBaseUrl)))
                .addSecurityItem(new SecurityRequirement().addList(schemeName));
    }
}