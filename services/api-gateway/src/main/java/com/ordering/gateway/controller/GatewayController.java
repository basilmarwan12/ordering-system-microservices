package com.ordering.gateway.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {

    @GetMapping("/")
    public Map<String, Object> index() {
        return Map.of(
                "service", "api-gateway",
                "status", "UP",
                "routes", Map.of(
                        "auth", "/auth/**",
                        "users", "/users/**",
                        "products", "/products/**",
                        "orders", "/orders/**"
                )
        );
    }
}
