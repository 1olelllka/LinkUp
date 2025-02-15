package com.olelllka.feed_service.service;

import com.olelllka.feed_service.domain.dto.NewPostEvent;
import com.olelllka.feed_service.domain.dto.ProfileDto;
import com.olelllka.feed_service.feign.ProfileInterface;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
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
@Log
public class RabbitListenerImpl {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProfileInterface profileInterface;
    public static final String queue = "feed_updates_queue";
    public static final String delete_queue = "delete_profile_queue_feed";

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
                redisTemplate.opsForList().leftPush("feed:profile:"+SHA256.hash(follower.getId().toString()), postId);
                redisTemplate.expire("feed:profile:" + SHA256.hash(follower.getId().toString()), Duration.ofHours(24)); // Set TTL to 1 day
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    // TODO (in future): Implement fallback strategy, so that followers don't lose new posts
    @CircuitBreaker(name = "feed-service", fallbackMethod = "rabbitFallbackMethod")
    private Page<ProfileDto> getFollowers(UUID authorId, Pageable pageable) {
        return profileInterface.getAllFollowersForProfile(authorId, pageable).getBody();
    }

    private Page<ProfileDto> rabbitFallbackMethod(UUID authorId, Pageable pageable, Throwable e) {
        log.warning("Failed to fetch followers for profile " + authorId + ": " + e.getMessage());
        return Page.empty();
    }

    @RabbitListener(queues = delete_queue)
    public void handleDeleteProfile(UUID profileId) {
        redisTemplate.delete("feed:profile:"+SHA256.hash(profileId.toString()));
    }
}
