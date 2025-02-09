package com.olelllka.feed_service.service;

import com.olelllka.feed_service.TestDataUtil;
import com.olelllka.feed_service.domain.dto.NewPostEvent;
import com.olelllka.feed_service.domain.dto.ProfileDto;
import com.olelllka.feed_service.feign.ProfileInterface;
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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RabbitListenerUnitTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ProfileInterface profileInterface;
    public static final String queue = "feed_updates_queue";
    @InjectMocks
    private RabbitListenerImpl rabbitListener;

    @Test
    public void testThatRabbitListenerWorksAsExpected() {
        // given
        UUID profileId = UUID.randomUUID();
        NewPostEvent postEvent = TestDataUtil.createNewPostEvent(profileId);
        Pageable pageable = PageRequest.of(0, 20);
        Pageable pageable2 = PageRequest.of(1, 20);
        ProfileDto profileDto = TestDataUtil.createProfileDto(profileId);
        Page<ProfileDto> expectedPage = new PageImpl<>(List.of(profileDto));
        Page<ProfileDto> emptyPage = new PageImpl<>(List.of());
        ListOperations<String, String> listOperations = mock(ListOperations.class);
        // when
        when(profileInterface.getAllFollowersForProfile(profileId, pageable)).thenReturn(ResponseEntity.ok(expectedPage));
        when(profileInterface.getAllFollowersForProfile(profileId, pageable2)).thenReturn(ResponseEntity.ok(emptyPage));
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.leftPush(anyString(), anyString())).thenReturn(1L);
        rabbitListener.handleNewPost(postEvent);
        // then
        verify(listOperations, times(1)).leftPush(eq("feed:profile:" + profileDto.getId()), eq(postEvent.getPostId()));
    }

}
