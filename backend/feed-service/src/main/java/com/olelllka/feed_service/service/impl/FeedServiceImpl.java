package com.olelllka.feed_service.service.impl;

import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log
public class FeedServiceImpl implements FeedService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Page<PostDto> getFeedForProfile(UUID profileId, Pageable pageable) {
        int start = (pageable.getPageNumber() + 1) * pageable.getPageSize();
        int end = start + pageable.getPageSize() - 1;
        List<String> postIds = redisTemplate.opsForList().range("feed:profile:"+profileId.toString(), start, end);
        log.info("" + postIds);
        return mockGetPostsByIds(postIds, start, end);
    }

    // it'll be here until I connect components to eureka, I think it's not that hard to integrate it here.
    private Page<PostDto> mockGetPostsByIds(List<String> ids, int start, int end) {
        return new PageImpl<>(List.of());
    }
}
