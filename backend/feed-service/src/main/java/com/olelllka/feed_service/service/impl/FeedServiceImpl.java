package com.olelllka.feed_service.service.impl;

import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.feign.PostsInterface;
import com.olelllka.feed_service.rest.exception.AuthException;
import com.olelllka.feed_service.service.FeedService;
import com.olelllka.feed_service.service.JWTUtil;
import com.olelllka.feed_service.service.SHA256;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log
public class FeedServiceImpl implements FeedService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostsInterface postsInterface;
    private final JWTUtil jwtUtil;

    @Override
    @CircuitBreaker(name = "feed-service", fallbackMethod = "fallbackMethod")
    public Page<PostDto> getFeedForProfile(UUID profileId, Pageable pageable, String jwt) {
        try {
            if (!profileId.toString().equals(jwtUtil.extractId(jwt))) {
                throw new AuthException("You're unauthorized to perform this action.");
            }
        } catch (SignatureException ex) {
            throw new AuthException(ex.getMessage());
        }
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = start + pageable.getPageSize() - 1;
        List<String> postIds = redisTemplate.opsForList().range("feed:profile:"+ SHA256.hash(profileId.toString()), start, end);
        return getPostsByIds(postIds.stream().map(Integer::parseInt).toList(), start, end);
    }

    // here can be some latency issues if list of ids is too large, but I'll fix it sometime later
    private Page<PostDto> getPostsByIds(List<Integer> ids, int start, int end) {
        List<PostDto> posts = new ArrayList<>();
        for (Integer id : ids) {
            ResponseEntity<PostDto> result = postsInterface.getPosts(id);
            if (result.getStatusCode().is2xxSuccessful()) {
                posts.add(result.getBody());
            } else if (result.getStatusCode().is5xxServerError()) {
                throw new RuntimeException("Post Service encounters error.");
            }
        }
        return new PageImpl<>(posts);
    }

    private Page<PostDto> fallbackMethod(UUID profileId, Pageable pageable, String jwt, Throwable t) {
        log.warning("Circuit Breaker triggered: " + t.getMessage());
        return Page.empty();
    }
}
