package com.olelllka.feed_service.service.impl;

import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.feign.PostsInterface;
import com.olelllka.feed_service.feign.ProfileInterface;
import com.olelllka.feed_service.rest.exception.NotFoundException;
import com.olelllka.feed_service.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PostsInterface postsInterface;
    private final ProfileInterface profileInterface;

    @Override
    public Page<PostDto> getFeedForProfile(UUID profileId, Pageable pageable) {
        if (!profileInterface.getProfileById(profileId).getStatusCode().is2xxSuccessful()) {
            throw new NotFoundException("User with such id does not exist");
        }
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = start + pageable.getPageSize() - 1;
        List<String> postIds = redisTemplate.opsForList().range("feed:profile:"+profileId, start, end);
        return getPostsByIds(postIds.stream().map(Integer::parseInt).toList(), start, end);
    }

    // here can be some latency issues if list of ids is too large, but I'll fix it sometime later
    private Page<PostDto> getPostsByIds(List<Integer> ids, int start, int end) {
        List<PostDto> posts = new ArrayList<>();
        for (Integer id : ids) {
            ResponseEntity<PostDto> result = postsInterface.getPosts(id);
            if (result.getStatusCode().is2xxSuccessful()) {
                posts.add(result.getBody());
            }
        }
        return new PageImpl<>(posts);
    }
}
