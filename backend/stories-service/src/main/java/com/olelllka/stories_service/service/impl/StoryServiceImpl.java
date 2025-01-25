package com.olelllka.stories_service.service.impl;

import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.feign.ProfileFeign;
import com.olelllka.stories_service.repository.StoryRepository;
import com.olelllka.stories_service.rest.exception.NotFoundException;
import com.olelllka.stories_service.service.StoryService;
import lombok.RequiredArgsConstructor;
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
public class StoryServiceImpl implements StoryService {

    private final StoryRepository repository;
    private final ProfileFeign profileService;

    @Override
    public Page<StoryEntity> getStoriesForUser(UUID id, Pageable pageable) {
        if (!profileService.getProfileById(id).getStatusCode().is2xxSuccessful()) {
            throw new NotFoundException("User with such id does not exist.");
        }
        return repository.findStoryByUserId(id, pageable);
    }

    @Override
    @Cacheable(value = "story", keyGenerator = "sha256KeyGenerator")
    public StoryEntity getSpecificStory(String storyId) {
        return repository.findById(storyId).orElseThrow(() -> new NotFoundException("Story with such id was not found."));
    }

    @Override
    public StoryEntity createStory(UUID userId, StoryEntity entity) {
        if (!profileService.getProfileById(userId).getStatusCode().is2xxSuccessful()) {
            throw new NotFoundException("User with such id does not exist.");
        }
        entity.setAvailable(true);
        entity.setUserId(userId);
        return repository.save(entity);
    }

    @Override
    @CachePut(value = "story", keyGenerator = "sha256KeyGenerator")
    public StoryEntity updateSpecificStory(String storyId, StoryEntity entity) {
        return repository.findById(storyId).map(story -> {
            Optional.ofNullable(entity.getImage()).ifPresent(story::setImage);
            entity.setAvailable(true);
            entity.setCreatedAt(new Date());
            return repository.save(story);
        }).orElseThrow(() -> new NotFoundException("Story with such id was not found."));
    }

    @Override
    @CacheEvict(value = "story", keyGenerator = "sha256KeyGenerator")
    public void deleteSpecificStory(String storyId) {
        repository.deleteById(storyId);
    }
}
