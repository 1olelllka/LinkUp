package com.olelllka.feed_service.service.impl;

import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.feign.PostsInterface;
import com.olelllka.feed_service.feign.ProfileInterface;
import com.olelllka.feed_service.rest.exception.NotFoundException;
import com.olelllka.feed_service.service.FeedService;
import com.olelllka.feed_service.service.SHA256;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
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
    private final ProfileInterface profileInterface;

    @Override
    @CircuitBreaker(name = "feed-service", fallbackMethod = "fallbackMethod")
    public Page<PostDto> getFeedForProfile(UUID profileId, Pageable pageable) {
        ResponseEntity<?> profileResponse = profileInterface.getProfileById(profileId);
        if (profileResponse.getStatusCode() == HttpStatus.NOT_FOUND) { // business logic
            throw new NotFoundException("User with such id does not exist");
        }
        if (!profileResponse.getStatusCode().is2xxSuccessful()) { // another errors
            throw new RuntimeException("Profile Service Failure: " + profileResponse.getStatusCode());
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
            }
        }
        return new PageImpl<>(posts);
    }

    private Page<PostDto> fallbackMethod(UUID profileId, Pageable pageable, Throwable t) {
        if (t instanceof NotFoundException) {
            throw new NotFoundException(t.getMessage());
        }
        log.warning("Circuit Breaker triggered: " + t.getMessage());
        return Page.empty();
    }
}
