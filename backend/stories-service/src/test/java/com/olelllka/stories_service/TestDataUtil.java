package com.olelllka.stories_service;

import com.olelllka.stories_service.domain.entity.StoryEntity;

public class TestDataUtil {

    public static StoryEntity createStoryEntity() {
        return StoryEntity.builder()
                .image("Test Image Url")
                .build();
    }
}
