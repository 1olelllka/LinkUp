package com.olelllka.stories_service.rest.controller;

import com.olelllka.stories_service.domain.dto.CreateStoryDto;
import com.olelllka.stories_service.domain.dto.ErrorDto;
import com.olelllka.stories_service.domain.dto.StoryDto;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.mapper.StoryMapper;
import com.olelllka.stories_service.rest.exception.ValidationException;
import com.olelllka.stories_service.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Stories Service", description = "API Endpoints for stories service")
public class StoryController {

    private final StoryService service;
    private final StoryMapper<StoryEntity, StoryDto> mapper;

    @Operation(summary = "Get all stories for specific user (stories archive)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched archive"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
            })
    })
    @GetMapping("/archive/{user_id}")
    public ResponseEntity<Page<StoryDto>> getArchivedStoriesForUser(@PathVariable UUID user_id,
                                                                    @RequestHeader(name="Authorization") String header,
                                                                    Pageable pageable) {
        Page<StoryEntity> entities = service.getArchiveForUser(user_id, header.substring(7), pageable);
        Page<StoryDto> result = entities.map(mapper::toDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Get stories feed for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched feed"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
            })
    })
    @GetMapping("/users/{user_id}")
    public ResponseEntity<Page<StoryDto>> getStoriesFeedForUser(@PathVariable UUID user_id,
                                                                @RequestHeader(name="Authorization") String header,
                                                                Pageable pageable) {
        Page<StoryEntity> entities = service.getStoriesFeed(user_id, header.substring(7), pageable);
        Page<StoryDto> result = entities.map(mapper::toDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Create new story")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created story. Created story will be sent to RabbitMQ for populating feeds"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
            }),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "User not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
            })
    })
    @PostMapping("/users/{user_id}")
    public ResponseEntity<StoryDto> createNewStoryForUser(@PathVariable UUID user_id,
                                                          @RequestHeader(name="Authorization") String header,
                                                          @RequestBody @Valid  CreateStoryDto dto,
                                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        StoryEntity entity = StoryEntity.builder().image(dto.getImage()).build();
        StoryEntity saved = service.createStory(user_id, entity, header.substring(7));
        return new ResponseEntity<>(mapper.toDto(saved), HttpStatus.CREATED);
    }

//    @Operation(summary = "---deprecated---")
//    @GetMapping("/{story_id}")
//    public ResponseEntity<StoryDto> getSpecificStory(@PathVariable String story_id,
//                                                     @RequestHeader(name = "Authorization") String header) {
//        StoryEntity entity = service.getSpecificStory(story_id, header.substring(7));
//        return new ResponseEntity<>(mapper.toDto(entity), HttpStatus.OK);
//    }

    @Operation(summary = "Update story")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated story. Updated story will be sent to RabbitMQ for populating feeds"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Story not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
            })
    })
    @PatchMapping("/{story_id}")
    public ResponseEntity<StoryDto> updateSpecificStory(@PathVariable String story_id,
                                                        @RequestHeader(name="Authorization") String header,
                                                        @RequestBody @Valid CreateStoryDto dto,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        StoryEntity entity = StoryEntity.builder().image(dto.getImage()).build();
        StoryEntity saved = service.updateSpecificStory(story_id, entity, header.substring(7));
        return new ResponseEntity<>(mapper.toDto(saved), HttpStatus.OK);
    }

    @Operation(summary = "Delete story")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted story"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class))
            })
    })
    @DeleteMapping("/{story_id}")
    public ResponseEntity deleteSpecificStory(@PathVariable String story_id,
                                              @RequestHeader(name="Authorization") String header) {
        service.deleteSpecificStory(story_id, header.substring(7));
        return new ResponseEntity(null, HttpStatus.NO_CONTENT);
    }
}
