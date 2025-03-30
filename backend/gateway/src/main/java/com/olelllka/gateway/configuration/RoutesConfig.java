package com.olelllka.gateway.configuration;

import com.olelllka.gateway.service.RateLimitingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RoutesConfig {

    private final RateLimitingFilter rateLimitingFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("posts-service",
                        r -> r.path("/api/posts/**")
                                .filters(f -> f.stripPrefix(1)
                                        .filter(rateLimitingFilter)
                                        .circuitBreaker(c -> c.setName("posts-service").setFallbackUri("forward:/fallback")))
                                .uri("http://localhost:8000")) // here localhost, bc it's django, I'll fix it later
                .route("profile-service",
                        r -> r.path("/api/profiles/**")
                                .filters(f -> f.stripPrefix(1)
                                        .filter(rateLimitingFilter)
                                        .circuitBreaker(c -> c.setName("profile-auth-and-feed-services").setFallbackUri("forward:/fallback")))
                                .uri("lb://profile-service"))
                .route("auth-service",
                        r -> r.path("/api/auth/**")
                                .filters(f -> f.stripPrefix(1)
                                        .filter(rateLimitingFilter)
                                        .circuitBreaker(c -> c.setName("profile-auth-and-feed-services").setFallbackUri("forward:/fallback")))
                                .uri("lb://auth-service"))
                .route("feed-service",
                        r -> r.path("/api/feeds/**")
                                .filters(f -> f.stripPrefix(1)
                                        .filter(rateLimitingFilter)
                                        .circuitBreaker(c -> c.setName("profile-auth-and-feed-services").setFallbackUri("forward:/fallback")))
                                .uri("lb://feed-service"))
                .route("stories-service",
                        r -> r.path("/api/stories/**")
                                .filters(f -> f.stripPrefix(1)
                                        .filter(rateLimitingFilter)
                                        .circuitBreaker(c -> c.setName("stories-service").setFallbackUri("forward:/fallback")))
                                .uri("lb://stories-service"))
                .route("chat-service",
                        r -> r.path("/api/chats/**")
                                .filters(f -> f.stripPrefix(1)
                                        .filter(rateLimitingFilter)
                                        .circuitBreaker(c -> c.setName("chat-service").setFallbackUri("forward:/fallback")))
                                .uri("lb://chat-service"))
                .route("chat-websocket-service",
                        r -> r.path("/chat")
                                .filters(f -> f.circuitBreaker(c -> c.setName("chat-service").setFallbackUri("forward:/fallback")))
                                .uri("lb:ws://chat-service"))
                .route("notification-service",
                        r -> r.path("/api/notifications/**")
                                .filters(f -> f.stripPrefix(1)
                                        .filter(rateLimitingFilter)
                                        .circuitBreaker(c -> c.setName("notification-service").setFallbackUri("forward:/fallback")))
                                .uri("lb://notification-service"))
                .route("notification-websocket-service",
                        r -> r.path("/notifications")
                                .filters(f -> f.circuitBreaker(c -> c.setName("notification-service").setFallbackUri("forward:/fallback")))
                                .uri("lb:ws://notification-service"))
                .build();
    }

}
