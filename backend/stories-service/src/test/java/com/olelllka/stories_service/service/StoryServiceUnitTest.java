package com.olelllka.stories_service.service;

import com.olelllka.stories_service.TestDataUtil;
import com.olelllka.stories_service.domain.dto.StoryDto;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.feign.ProfileFeign;
import com.olelllka.stories_service.mapper.StoryMapper;
import com.olelllka.stories_service.repository.StoryRepository;
import com.olelllka.stories_service.rest.exception.AuthException;
import com.olelllka.stories_service.rest.exception.NotFoundException;
import com.olelllka.stories_service.service.impl.StoryServiceImpl;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoryServiceUnitTest {

    @Mock
    private StoryRepository repository;
    @Mock
    private ProfileFeign profileFeign;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private StoryMapper<StoryEntity, StoryDto> mapper;
    @Mock
    private MessagePublisher messagePublisher;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @InjectMocks
    private StoryServiceImpl service;

    @Test
    public void testThatGetArchiveForUserReturnsPageOfResults() {
        // given
        UUID id = UUID.randomUUID();
        String jwt = "jwt";
        Pageable pageable = PageRequest.of(0, 1);
        StoryEntity entity = TestDataUtil.createStoryEntity();
        Page<StoryEntity> expected = new PageImpl<>(List.of(entity));
        // when
        when(repository.findStoryByUserId(id, pageable)).thenReturn(expected);
        when(jwtUtil.extractId(jwt)).thenReturn(id.toString());
        Page<StoryEntity> result = service.getArchiveForUser(id, jwt, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().get(0).getImage(), expected.getContent().get(0).getImage())
        );
        verify(repository, times(1)).findStoryByUserId(id, pageable);
    }

    @Test
    public void testThatGetArchiveForUserThrowsAuthException() {
        // given
        UUID id = UUID.randomUUID();
        String jwt = "jwt";
        Pageable pageable = PageRequest.of(0, 1);
        // when
        when(jwtUtil.extractId(jwt)).thenReturn("incorrect");
        assertThrows(AuthException.class, () -> service.getArchiveForUser(id, jwt, pageable));
        // then
        verify(repository, never()).findStoryByUserId(id, pageable);
    }

    @Test
    public void testThatCreateStoryWorksWell() {
        // given
        UUID userId = UUID.randomUUID();
        StoryEntity expected = TestDataUtil.createStoryEntity();
        expected.setAvailable(true);
        expected.setUserId(userId);
        StoryEntity story = TestDataUtil.createStoryEntity();
        StoryDto mappedDto = StoryDto.builder()
                .id("TEST_ID").build();
        String jwt = "jwt";
        // when
        when(repository.save(expected)).thenReturn(story);
        when(jwtUtil.extractId(jwt)).thenReturn(userId.toString());
        when(mapper.toDto(story)).thenReturn(mappedDto);
        when(profileFeign.getProfileById(userId)).thenReturn(ResponseEntity.ok().build());
        StoryEntity result = service.createStory(userId, story, jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getImage(), expected.getImage()),
                () -> assertEquals(result.getAvailable(), expected.getAvailable()),
                () -> assertEquals(result.getUserId(), expected.getUserId())
        );
        verify(repository, times(1)).save(expected);
        verify(messagePublisher, times(1)).sendCreatedStory(mappedDto);
    }

    @Test
    public void testThatGettingStoriesFeedThrowsAnException() {
        UUID id = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        when(jwtUtil.extractId("jwt")).thenReturn(UUID.randomUUID().toString());
        assertThrows(AuthException.class, () -> service.getStoriesFeed(id, "jwt", pageable));
        verify(redisTemplate, never()).hasKey(anyString());
    }

    @Test
    public void testThatGettingStoriesFeedReturnEmptyPageOfStories() {
        UUID id = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        when(jwtUtil.extractId("jwt")).thenReturn(id.toString());
        when(redisTemplate.hasKey("story-feed:" + SHA256.generate(id.toString()))).thenReturn(false);
        Page<StoryEntity> page = service.getStoriesFeed(id, "jwt", pageable);
        assertEquals(0, page.getContent().size());
        verify(redisTemplate, never()).opsForList();
    }

    @Test
    public void testThatGettingStoriesFeedReturnPageOfStories() {
        UUID id = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        String key = "story-feed:" + SHA256.generate(id.toString());
        when(jwtUtil.extractId("jwt")).thenReturn(id.toString());
        when(redisTemplate.hasKey(key)).thenReturn(true);
        when(redisTemplate.opsForList()).thenReturn(mock(ListOperations.class));
        when(redisTemplate.opsForList().size(key)).thenReturn(1L);
        when(redisTemplate.opsForList().range(key, 0, 1)).thenReturn(List.of("1"));
        when(repository.findByIdsAndByAvailable(List.of("1"), pageable)).thenReturn(new PageImpl<>(
                List.of(TestDataUtil.createStoryEntity()), pageable, 1));
        Page<StoryEntity> page = service.getStoriesFeed(id, "jwt", pageable);
        assertEquals(1, page.getTotalElements());
    }

    @Test
    public void testThatCreateStoryThrowsExceptions () {
        UUID userId = UUID.randomUUID();
        StoryEntity story = TestDataUtil.createStoryEntity();
        String jwt = "jwt";
        when(profileFeign.getProfileById(userId)).thenReturn(ResponseEntity.notFound().build());
        when(jwtUtil.extractId(jwt)).thenReturn(userId.toString());
        assertThrows(NotFoundException.class, () -> service.createStory(userId, story, jwt));
        verify(repository, never()).save(any(StoryEntity.class));
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        assertThrows(AuthException.class, () -> service.createStory(userId, story, jwt));
    }

    @Test
    public void testThatUpdateStoryThrowsNotFoundException() {
        // given
        String storyId = "1234";
        StoryEntity entity = TestDataUtil.createStoryEntity();
        String jwt = "jwt";
        // when
        when(repository.findById(storyId)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.updateSpecificStory(storyId, entity, jwt));
        verify(repository, never()).save(any(StoryEntity.class));
    }

    @Test
    public void testThatUpdateStoryThrowsAuthException() {
        // given
        String storyId = "1234";
        StoryEntity entity = TestDataUtil.createStoryEntity();
        entity.setUserId(UUID.randomUUID());
        String jwt = "jwt";
        // when
        when(repository.findById(storyId)).thenReturn(Optional.of(entity));
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        // then
        assertThrows(AuthException.class, () -> service.updateSpecificStory(storyId, entity, jwt));
        verify(repository, never()).save(any(StoryEntity.class));
    }

    @Test
    public void testThatUpdateStoryReturnsUpdatedStory() {
        // given
        String storyId = "1234";
        String jwt = "jwt";
        UUID userId = UUID.randomUUID();
        StoryEntity found = TestDataUtil.createStoryEntity();
        found.setUserId(userId);
        StoryEntity updated = TestDataUtil.createStoryEntity();
        updated.setImage("UPDATED");
        StoryEntity expected = TestDataUtil.createStoryEntity();
        expected.setImage(updated.getImage());
        expected.setAvailable(true);
        // when
        when(repository.findById(storyId)).thenReturn(Optional.of(found));
        when(repository.save(any(StoryEntity.class))).thenReturn(expected); // if I put expected in save it'll throw error 'coz of date
        when(jwtUtil.extractId(jwt)).thenReturn(found.getUserId().toString());
        StoryEntity result = service.updateSpecificStory(storyId, updated, jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getImage(), expected.getImage()),
                () -> assertEquals(result.getAvailable(), expected.getAvailable()),
                () -> assertNotNull(result.getCreatedAt())
        );
        verify(repository, times(1)).findById(storyId);
        verify(repository, times(1)).save(any(StoryEntity.class));

    }

    @Test
    public void testThatDeleteStoryThrowsAuthException() {
        // given
        String id = "1234";
        StoryEntity entity = TestDataUtil.createStoryEntity();
        String jwt = "jwt";
        entity.setUserId(UUID.randomUUID());
        // when
        when(repository.existsById(id)).thenReturn(true);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        // then
        assertThrows(AuthException.class, () -> service.deleteSpecificStory(id, jwt));
        verify(repository, never()).deleteById(anyString());
    }

    @Test
    public void testThatDeleteStoryWorksWellWhenStoryDoesNotExist() {
        // given
        String id = "1234";
        // when
        when(repository.existsById(id)).thenReturn(false);
        service.deleteSpecificStory(id, "jwt");
        // then
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    public void testThatDeleteStoryWorksWellWhenStoryExists() {
        // given
        String id = "1234";
        String jwt = "jwt";
        StoryEntity entity = TestDataUtil.createStoryEntity();
        entity.setUserId(UUID.randomUUID());
        // when
        when(repository.existsById(id)).thenReturn(true);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(jwtUtil.extractId(jwt)).thenReturn(entity.getUserId().toString());
        // then
        service.deleteSpecificStory(id, jwt);
        verify(repository, times(1)).deleteById(id);
    }
}
