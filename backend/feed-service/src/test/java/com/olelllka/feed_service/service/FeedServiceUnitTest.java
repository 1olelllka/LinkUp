package com.olelllka.feed_service.service;

import com.olelllka.feed_service.TestDataUtil;
import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.feign.PostsInterface;
import com.olelllka.feed_service.feign.ProfileInterface;
import com.olelllka.feed_service.rest.exception.NotFoundException;
import com.olelllka.feed_service.service.impl.FeedServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedServiceUnitTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private PostsInterface postsInterface;
    @Mock
    private ProfileInterface profileInterface;
    @InjectMocks
    private FeedServiceImpl feedService;

    @Test
    public void testThatGetFeedForUserThrowsException() {
        // given
        UUID profileId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        // when
        when(profileInterface.getProfileById(profileId)).thenReturn(ResponseEntity.notFound().build());
        // then
        assertThrows(NotFoundException.class, () -> feedService.getFeedForProfile(profileId, pageable));
        verify(redisTemplate, never()).opsForList();
    }

    @Test
    public void testThatGetFeedForUserReturnsEmptyPage() {
        UUID profileId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        Page<PostDto> expected = new PageImpl<>(List.of());
        // when
        when(profileInterface.getProfileById(profileId)).thenReturn(ResponseEntity.ok().build());
        when(redisTemplate.opsForList()).thenReturn(mock(ListOperations.class));
        when(redisTemplate.opsForList().range("feed:profile:"+SHA256.hash(profileId.toString()), 0, 0)).thenReturn(List.of());
        Page<PostDto> result = feedService.getFeedForProfile(profileId, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), expected.getTotalElements())
        );
        verify(postsInterface, never()).getPosts(anyInt());
    }


    @Test
    public void testThatGetFeedForUserWorksAsExpected() {
        // given
        UUID profileId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        PostDto postDto = TestDataUtil.createPostDto(profileId);
        Page<PostDto> expected = new PageImpl<>(List.of(postDto));
        // when
        when(profileInterface.getProfileById(profileId)).thenReturn(ResponseEntity.ok().build());
        when(redisTemplate.opsForList()).thenReturn(mock(ListOperations.class));
        when(redisTemplate.opsForList().range("feed:profile:"+SHA256.hash(profileId.toString()), 0, 0)).thenReturn(List.of("1"));
        when(postsInterface.getPosts(1)).thenReturn(ResponseEntity.ok(postDto));
        Page<PostDto> result = feedService.getFeedForProfile(profileId, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().getFirst().getUser_id(), expected.getContent().getFirst().getUser_id())
        );
    }
}
