package com.olelllka.stories_service.rest.controller;

import com.olelllka.stories_service.domain.dto.CreateStoryDto;
import com.olelllka.stories_service.domain.dto.StoryDto;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.mapper.StoryMapper;
import com.olelllka.stories_service.rest.exception.ValidationException;
import com.olelllka.stories_service.service.StoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService service;
    private final StoryMapper<StoryEntity, StoryDto> mapper;

    @GetMapping("/users/{user_id}")
    public ResponseEntity<Page<StoryDto>> getAllStoriesForUser(@PathVariable UUID user_id,
                                                               Pageable pageable) {
        Page<StoryEntity> entities = service.getStoriesForUser(user_id, pageable);
        Page<StoryDto> result = entities.map(mapper::toDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/users/{user_id}")
    public ResponseEntity<StoryDto> createNewStoryForUser(@PathVariable UUID user_id,
                                                          @RequestBody @Valid  CreateStoryDto dto,
                                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        StoryEntity entity = StoryEntity.builder().image(dto.getImage()).build();
        StoryEntity saved = service.createStory(user_id, entity);
        return new ResponseEntity<>(mapper.toDto(saved), HttpStatus.CREATED);
    }

    @GetMapping("/{story_id}")
    public ResponseEntity<StoryDto> getSpecificStory(@PathVariable String story_id) {
        StoryEntity entity = service.getSpecificStory(story_id);
        return new ResponseEntity<>(mapper.toDto(entity), HttpStatus.OK);
    }

    @PatchMapping("/{story_id}")
    public ResponseEntity<StoryDto> updateSpecificStory(@PathVariable String story_id,
                                                        @RequestBody @Valid CreateStoryDto dto,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        StoryEntity entity = StoryEntity.builder().image(dto.getImage()).build();
        StoryEntity saved = service.updateSpecificStory(story_id, entity);
        return new ResponseEntity<>(mapper.toDto(saved), HttpStatus.OK);
    }

    @DeleteMapping("/{story_id}")
    public ResponseEntity deleteSpecificStory(@PathVariable String story_id) {
        service.deleteSpecificStory(story_id);
        return new ResponseEntity(null, HttpStatus.NO_CONTENT);
    }
}
