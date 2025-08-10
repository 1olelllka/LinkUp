package com.olelllka.feed_service.rest.controller;

import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/{profile_id}")
    public ResponseEntity<Page<PostDto>> getFeedForSpecificProfile(@PathVariable UUID profile_id,
                                                                   @PageableDefault(size=1) Pageable pageable,
                                                                   @RequestHeader(name="Authorization") String header) {
        Page<PostDto> result = feedService.getFeedForProfile(profile_id, pageable, header.substring(7));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
