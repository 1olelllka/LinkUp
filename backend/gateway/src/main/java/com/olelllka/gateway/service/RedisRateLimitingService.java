package com.olelllka.gateway.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisRateLimitingService {
    private final RedisTemplate<String, Integer> redisTemplate;
    private final int TIME_WINDOW = 60;
    // I set 60 requests per minute for every service for now. In future this will be adjusted
    private final Map<String, Integer> rateLimits = Map.of(
            "/api/auth", 60,
            "/api/posts", 60,
            "/api/profiles", 60,
            "/api/feeds", 60,
            "/api/stories", 60,
            "/api/chats", 60,
            "/api/notifications", 60
    );

    public RedisRateLimitingService(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String clientIp, String route) {
        String key = "rate_limit:" + SHA256.hash(clientIp) + ":route:" + route;

        if (!redisTemplate.hasKey(key)) {
            redisTemplate.opsForValue().set(key, 1, TIME_WINDOW, TimeUnit.SECONDS); // Initialize counter with expiration
            return true;
        }
        Integer count = redisTemplate.opsForValue().get(key);
        if (count < rateLimits.get(route)) {
            redisTemplate.opsForValue().increment(key, 1);
            return true;
        } else {
            return false;
        }
    }
}
