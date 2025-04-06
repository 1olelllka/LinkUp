package com.olelllka.stories_service.service;

import com.olelllka.stories_service.domain.entity.StoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoryService {
    Page<StoryEntity> getStoriesForUser(UUID id, String jwt, Pageable pageable);

    StoryEntity getSpecificStory(String storyId, String jwt);

    StoryEntity createStory(UUID userId, StoryEntity entity, String jwt);

    StoryEntity updateSpecificStory(String storyId, StoryEntity entity, String jwt);

    void deleteSpecificStory(String storyId, String jwt);
}
