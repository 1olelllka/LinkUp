package com.olelllka.stories_service.service;

import com.olelllka.stories_service.domain.entity.StoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoryService {
    Page<StoryEntity> getStoriesForUser(UUID id, Pageable pageable);

    StoryEntity getSpecificStory(String storyId);

    StoryEntity createStory(UUID userId, StoryEntity entity);

    StoryEntity updateSpecificStory(String storyId, StoryEntity entity);

    void deleteSpecificStory(String storyId);
}
