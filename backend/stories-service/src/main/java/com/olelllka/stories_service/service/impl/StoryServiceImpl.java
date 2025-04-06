package com.olelllka.stories_service.service.impl;

import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.feign.ProfileFeign;
import com.olelllka.stories_service.repository.StoryRepository;
import com.olelllka.stories_service.rest.exception.AuthException;
import com.olelllka.stories_service.rest.exception.NotFoundException;
import com.olelllka.stories_service.service.JWTUtil;
import com.olelllka.stories_service.service.StoryService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log
public class StoryServiceImpl implements StoryService {

    private final StoryRepository repository;
    private final ProfileFeign profileService;
    private final JWTUtil jwtUtil;

    @Override
    public Page<StoryEntity> getStoriesForUser(UUID id, String jwt, Pageable pageable) {
        jwtCheck(jwt, id.toString());
        return repository.findStoryByUserId(id, pageable);
    }

    @Override
    @Cacheable(value = "story", keyGenerator = "sha256KeyGenerator")
    public StoryEntity getSpecificStory(String storyId, String jwt) {
        StoryEntity entity = repository.findById(storyId).orElseThrow(() -> new NotFoundException("Story with such id was not found."));
        try {
            if (!entity.getUserId().toString().equals(jwtUtil.extractId(jwt))) {
                if(!entity.getAvailable()) throw new AuthException("You're unauthorized to perform such operation.");
            }
        } catch (SignatureException ex) {
            throw new AuthException(ex.getMessage());
        }
        return entity;
    }

    @Override
    @CircuitBreaker(name = "stories-service", fallbackMethod = "createFallback")
    public StoryEntity createStory(UUID userId, StoryEntity entity, String jwt) {
        jwtCheck(jwt, userId.toString());
        if (!profileService.getProfileById(userId).getStatusCode().is2xxSuccessful()) {
            throw new NotFoundException("User with such id does not exist.");
        }
        entity.setAvailable(true);
        entity.setUserId(userId);
        return repository.save(entity);
    }

    @Override
    @CachePut(value = "story", keyGenerator = "sha256KeyGenerator")
    public StoryEntity updateSpecificStory(String storyId, StoryEntity entity, String jwt) {
        return repository.findById(storyId).map(story -> {
            jwtCheck(jwt, story.getUserId().toString());
            Optional.ofNullable(entity.getImage()).ifPresent(story::setImage);
            entity.setAvailable(true);
            entity.setCreatedAt(new Date());
            return repository.save(story);
        }).orElseThrow(() -> new NotFoundException("Story with such id was not found."));
    }

    @Override
    @CacheEvict(value = "story", keyGenerator = "sha256KeyGenerator")
    public void deleteSpecificStory(String storyId, String jwt) {
        if (repository.existsById(storyId)) {
            StoryEntity entity = repository.findById(storyId).get();
            if (!jwtUtil.extractId(jwt).equals(entity.getUserId().toString())) {
                throw new AuthException("You're unauthorized to perform such operation.");
            }
        }
        repository.deleteById(storyId);
    }

    private void jwtCheck(String jwt, String id) {
        try {
            if (!id.equals(jwtUtil.extractId(jwt))) {
                throw new AuthException("You're unauthorized to perform such operation.");
            }
        } catch (SignatureException ex) {
            throw new AuthException(ex.getMessage());
        }
    }

    private StoryEntity createFallback(UUID userId, StoryEntity entity, String jwt, Throwable t) {
        if (t instanceof NotFoundException) {
            throw new NotFoundException(t.getMessage());
        } else if (t instanceof AuthException) {
            throw new AuthException(t.getMessage());
        }
        log.warning("Circuit Breaker triggered: " + t.getMessage());
        return StoryEntity.builder()
                .id("circuit-breaker.id")
                .image("circuit-breaker.url")
                .available(false)
                .userId(UUID.randomUUID())
                .createdAt(new Date())
                .build();
    }
}
