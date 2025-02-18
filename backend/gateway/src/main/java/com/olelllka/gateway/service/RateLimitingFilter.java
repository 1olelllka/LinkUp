package com.olelllka.gateway.service;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Objects;

@Component
public class RateLimitingFilter implements GatewayFilter, Ordered {

    private final RedisRateLimitingService redisRateLimitingService;

    public RateLimitingFilter(RedisRateLimitingService redisRateLimitingService) {
        this.redisRateLimitingService = redisRateLimitingService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        String clientIp = Objects.requireNonNull(remoteAddress).getAddress().getHostAddress();
        String[] URI = exchange.getRequest().getURI().getPath().split("/");
        String completeURI = "/" + URI[1] + "/" + URI[2];
        if (!redisRateLimitingService.isAllowed(clientIp, completeURI)) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
