package com.olelllka.stories_service.mapper;

public interface StoryMapper<Entity, Dto> {

    Entity toEntity(Dto dto);

    Dto toDto(Entity entity);

}
