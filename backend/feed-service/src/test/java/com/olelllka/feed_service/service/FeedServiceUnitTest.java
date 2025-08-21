package com.olelllka.feed_service.service;

import com.olelllka.feed_service.TestDataUtil;
import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.feign.PostsInterface;
import com.olelllka.feed_service.rest.exception.AuthException;
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
    private JWTUtil jwtUtil;
    @InjectMocks
    private FeedServiceImpl feedService;

    @Test
    public void testThatGetFeedThrowsException() {
        UUID profileId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        String jwt = "jwt";
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        // then
        assertThrows(AuthException.class, () -> feedService.getFeedForProfile(profileId, pageable, jwt));
        verify(redisTemplate, never()).opsForList();
    }

    @Test
    public void testThatGetFeedForUserReturnsEmptyPage() {
        UUID profileId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        Page<PostDto> expected = new PageImpl<>(List.of());
        String jwt = "jwt";
        // when
        when(redisTemplate.opsForList()).thenReturn(mock(ListOperations.class));
        when(jwtUtil.extractId(jwt)).thenReturn(profileId.toString());
        when(redisTemplate.opsForList().range("feed:profile:"+SHA256.hash(profileId.toString()), 0, -1)).thenReturn(List.of());
        Page<PostDto> result = feedService.getFeedForProfile(profileId, pageable, jwt);
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
        String jwt = "jwt";
        // when
        when(redisTemplate.opsForList()).thenReturn(mock(ListOperations.class));
        when(redisTemplate.opsForList().range("feed:profile:"+SHA256.hash(profileId.toString()), 0, -1)).thenReturn(List.of("1"));
        when(postsInterface.getPosts(1)).thenReturn(ResponseEntity.ok(postDto));
        when(jwtUtil.extractId(jwt)).thenReturn(profileId.toString());
        Page<PostDto> result = feedService.getFeedForProfile(profileId, pageable, jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().getFirst().getUser_id(), expected.getContent().getFirst().getUser_id())
        );
    }
}
