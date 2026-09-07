package com.ordering.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver clientIpKeyResolver() {
        return exchange -> {
            if (exchange.getRequest().getRemoteAddress() == null
                    || exchange.getRequest().getRemoteAddress().getAddress() == null) {
                return Mono.error(new IllegalStateException("Unable to determine client IP address"));
            }
            return Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
        };
    }
}
