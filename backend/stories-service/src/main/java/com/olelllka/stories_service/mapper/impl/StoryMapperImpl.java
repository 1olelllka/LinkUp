package com.olelllka.stories_service.mapper.impl;

import com.olelllka.stories_service.domain.dto.StoryDto;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.mapper.StoryMapper;
import org.springframework.stereotype.Component;

@Component
public class StoryMapperImpl implements StoryMapper<StoryEntity, StoryDto> {

    @Override
    public StoryEntity toEntity(StoryDto storyDto) {
        return StoryEntity.builder()
                .id(storyDto.getId())
                .image(storyDto.getImage())
                .userId(storyDto.getUserId())
                .available(storyDto.getAvailable())
                .likes(storyDto.getLikes())
                .createdAt(storyDto.getCreatedAt())
                .build();
    }

    @Override
    public StoryDto toDto(StoryEntity storyEntity) {
        return StoryDto.builder()
                .id(storyEntity.getId())
                .image(storyEntity.getImage())
                .userId(storyEntity.getUserId())
                .available(storyEntity.getAvailable())
                .likes(storyEntity.getLikes())
                .createdAt(storyEntity.getCreatedAt())
                .build();
    }
}
