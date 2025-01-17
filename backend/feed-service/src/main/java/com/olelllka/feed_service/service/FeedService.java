package com.olelllka.feed_service.service;

import com.olelllka.feed_service.domain.dto.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FeedService {
    Page<PostDto> getFeedForProfile(UUID profileId, Pageable pageable);
}
