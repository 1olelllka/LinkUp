package com.olelllka.gateway.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("posts-service",
                        r -> r.path("/api/posts/**")
                                .filters(f -> f.stripPrefix(1))
                                .uri("http://localhost:8000")) // here localhost, bc it's django, I'll fix it later
                .route("profile-service",
                        r -> r.path("/api/profiles/**")
                                .filters(f -> f.stripPrefix(1))
                                .uri("lb://profile-service"))
                .route("feed-service",
                        r -> r.path("/api/feeds/**")
                                .filters(f -> f.stripPrefix(1))
                                .uri("lb://feed-service"))
                .route("stories-service",
                        r -> r.path("/api/stories/**")
                                .filters(f -> f.stripPrefix(1))
                                .uri("lb://stories-service"))
                .route("chat-service",
                        r -> r.path("/api/chats/**")
                                .filters(f -> f.stripPrefix(1))
                                .uri("lb://chat-service"))
                .route("chat-websocket-service",
                        r -> r.path("/chat")
                                .uri("lb:ws://chat-service"))
                .route("notification-service",
                        r -> r.path("/api/notifications/**")
                                .filters(f -> f.stripPrefix(1))
                                .uri("lb://notification-service"))
                .route("notification-websocket-service",
                        r -> r.path("/notifications")
                                .uri("lb:ws://notification-service"))
                .build();
    }

}
