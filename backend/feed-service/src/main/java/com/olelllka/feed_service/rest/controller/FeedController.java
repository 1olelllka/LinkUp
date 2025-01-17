package com.olelllka.feed_service.rest.controller;

import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/{profile_id}")
    public ResponseEntity<Page<PostDto>> getFeedForSpecificProfile(@PathVariable UUID profile_id, Pageable pageable) {
        Page<PostDto> result = feedService.getFeedForProfile(profile_id, pageable);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
