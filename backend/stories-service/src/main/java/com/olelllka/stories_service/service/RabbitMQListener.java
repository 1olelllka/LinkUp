package com.olelllka.stories_service.service;

import com.olelllka.stories_service.configuration.RabbitMQConfig;
import com.olelllka.stories_service.domain.dto.ProfileDto;
import com.olelllka.stories_service.domain.dto.StoryDto;
import com.olelllka.stories_service.feign.ProfileFeign;
import com.olelllka.stories_service.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Log
public class RabbitMQListener {

    private final String deleteProfileQueue = "delete_profile_queue_story";
    private final StoryRepository storyRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ProfileFeign profileInterface;

    @RabbitListener(queues = deleteProfileQueue)
    public void deleteStoriesForSpecificProfile(UUID profileId) {
        storyRepository.deleteByUserId(profileId);
    }

    @RabbitListener(queues = RabbitMQConfig.CREATE_STORY_QUEUE)
    public void addStoryToTemporaryStorage(StoryDto dto) {
        String storyId = dto.getId();
        UUID userId = dto.getUserId();
        int page = 0;
        int size = 20;
        Page<ProfileDto> followers;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        do {
            Pageable pageable = PageRequest.of(page, size);
            followers = getFollowers(userId, pageable);
            if (followers.isEmpty()) {
                break;
            }
            if (followers.hasContent()) {
                futures.add(processFollowerBatch(storyId, followers.getContent()));
            }
            page++;
        } while (followers.hasContent());
    }

    @Async
    public CompletableFuture<Void> processFollowerBatch(String postId, List<ProfileDto> followers) {
        for (ProfileDto follower : followers) {
            try {
                redisTemplate.opsForList().leftPush("story-feed:"+SHA256.generate(follower.getId().toString()), postId);
                redisTemplate.expire("story-feed:" + SHA256.generate(follower.getId().toString()), Duration.ofHours(24));
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private Page<ProfileDto> getFollowers(UUID authorId, Pageable pageable) {
        return profileInterface.getAllFolloweesForProfile(authorId, pageable).getBody();
    }
}
