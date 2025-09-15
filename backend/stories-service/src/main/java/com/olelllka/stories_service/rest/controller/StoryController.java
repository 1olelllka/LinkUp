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
                             