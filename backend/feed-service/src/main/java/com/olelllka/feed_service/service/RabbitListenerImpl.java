package com.olelllka.feed_service.service;

import com.olelllka.feed_service.domain.dto.NewPostEvent;
import com.olelllka.feed_service.domain.dto.ProfileDto;
import com.olelllka.feed_service.feign.ProfileInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class RabbitListenerImpl {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProfileInterface profileInterface;
    public static final String queue = "feed_updates_queue";

    @RabbitListener(queues = queue)
    public void handleNewPost(NewPostEvent postEvent) {
        String postId = postEvent.getPostId();
        UUID profileId = postEvent.getProfileId();
        int page = 0;
        int size = 20;
        Page<ProfileDto> followers;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        do {
            Pageable pageable = PageRequest.of(page, size);
            followers = getFollowers(profileId, pageable);
            if (followers.isEmpty()) {
                break;
            }
            if (followers.hasContent()) {
                futures.add(processFollowerBatch(postId, followers.getContent()));
            }
            page++;
        } while (followers.hasContent());
    }

    @Async
    public CompletableFuture<Void> processFollowerBatch(String postId, List<ProfileDto> followers) {
        for (ProfileDto follower : followers) {
            try {
                redisTemplate.opsForList().leftPush("feed:profile:"+follower.getId(), postId);
                redisTemplate.expire("feed:profile:" + follower, Duration.ofHours(12)); // Set TTL to 0.5 day
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private Page<ProfileDto> getFollowers(UUID authorId, Pageable pageable) {
        return profileInterface.getAllFollowersForProfile(authorId, pageable).getBody();
    }
}
