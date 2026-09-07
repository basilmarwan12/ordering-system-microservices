package com.ordering.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;

/**
 * Plain RestClient rather than OpenFeign/Spring Cloud — no need for the extra
 * dependency at this scale. Swap for Feign or a Resilience4j-wrapped client
 * once retry/circuit-breaker behavior actually matters.
 *
 * product-service requires a valid token on every route (including GET), so
 * this call forwards the caller's own Authorization header rather than
 * calling anonymously. This only works because getProduct() runs
 * synchronously on the same thread handling the original incoming HTTP
 * request (RequestContextHolder reads thread-local request state) -- if this
 * ever moves to an async/reactive call, the token has to be captured and
 * passed explicitly instead.
 */
@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${services.product-service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public record ProductSnapshot(Long id, String name, BigDecimal price, Integer stock) {}

    public ProductSnapshot getProduct(Long productId) {
        String authorizationHeader = currentAuthorizationHeader();

        return restClient.get()
                .uri("/products/{id}", productId)
                .headers(headers -> {
                    if (authorizationHeader != null) {
                        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
                    }
                })
                .retrieve()
                .body(ProductSnapshot.class);
    }

    private String currentAuthorizationHeader() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }
}