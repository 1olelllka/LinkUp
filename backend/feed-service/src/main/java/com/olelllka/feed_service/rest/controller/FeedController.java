package com.olelllka.feed_service.rest.controller;

import com.olelllka.feed_service.domain.dto.ErrorMessage;
import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.service.FeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/feeds")
@RequiredArgsConstructor
@Tag(name = "Feed Service API Endpoint (Requires Profile & Posts services available)")
public class FeedController {

    private final FeedService feedService;

    @Operation(summary = "Get feed for user")
  