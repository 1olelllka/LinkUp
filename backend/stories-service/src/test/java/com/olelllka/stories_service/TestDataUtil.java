package com.olelllka.stories_service;

import com.olelllka.stories_service.domain.dto.ProfileDto;
import com.olelllka.stories_service.domain.entity.StoryEntity;

import java.util.UUID;

public class TestDataUtil {

    public static StoryEntity createStoryEntity() {
        return StoryEntity.builder()
                .image("Test Image Url")
                .build();
    }

    public static ProfileDto createProfileDto() {
        return ProfileDto.builder()
                .id(UUID.randomUUID())
                .name("random name")
                .photo("")
                .username("random username")
                .build();
    }
}
