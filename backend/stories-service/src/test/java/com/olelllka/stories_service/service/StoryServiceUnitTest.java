package com.olelllka.stories_service.service;

import com.olelllka.stories_service.TestDataUtil;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.feign.ProfileFeign;
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
    @InjectMocks
    private StoryServiceImpl service;

    @Test
    public void testThatGetStoriesForUserReturnsPageOfResults() {
        // given
        UUID id = UUID.randomUUID();
        String jwt = "jwt";
        Pageable pageable = PageRequest.of(0, 1);
        StoryEntity entity = TestDataUtil.createStoryEntity();
        Page<StoryEntity> expected = new PageImpl<>(List.of(entity));
        // when
        when(repository.findStoryByUserId(id, pageable)).thenReturn(expected);
        when(jwtUtil.extractId(jwt)).thenReturn(id.toString());
        Page<StoryEntity> result = service.getStoriesForUser(id, jwt, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().get(0).getImage(), expected.getContent().get(0).getImage())
        );
        verify(repository, times(1)).findStoryByUserId(id, pageable);
    }

    @Test
    public void testThatGetStoriesForUserThrowsAuthException() {
        // given
        UUID id = UUID.randomUUID();
        String jwt = "jwt";
        Pageable pageable = PageRequest.of(0, 1);
        // when
        when(jwtUtil.extractId(jwt)).thenReturn("incorrect");
        assertThrows(AuthException.class, () -> service.getStoriesForUser(id, jwt, pageable));
        // then
        verify(repository, never()).findStoryByUserId(id, pageable);
    }

    @Test
    public void testThatGetSpecificStoryThrowsNotFoundException() {
        // given
        String id = "1234";
        String jwt = "jwt";
        // when
        when(repository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.getSpecificStory(id, jwt));
    }

    @Test
    public void testThatGetSpecificStoryThrowsAuthException() {
        // given
        String id = "1235";
        String jwt = "jwt";
        StoryEntity story = TestDataUtil.createStoryEntity();
        story.setUserId(UUID.randomUUID());
        story.setAvailable(false);
        // when
        when(repository.findById(id)).thenReturn(Optional.of(story));
        when(jwtUtil.extractId(jwt)).thenReturn("incorrect");
        // then
        assertThrows(AuthException.class, () -> service.getSpecificStory(id, jwt));
    }

    @Test
    public void testThatGetSpecificStoryReturnsStoryForAuthorized() {
        // given
        String id = "1234";
        String jwt = "jwt";
        StoryEntity story = TestDataUtil.createStoryEntity();
        story.setUserId(UUID.randomUUID());
        story.setAvailable(false);
        // when
        when(repository.findById(id)).thenReturn(Optional.of(story));
        when(jwtUtil.extractId(jwt)).thenReturn(story.getUserId().toString());
        StoryEntity result = service.getSpecificStory(id, jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getImage(), story.getImage())
        );
        verify(repository, times(1)).findById(id);
    }

    @Test
    public void testThatGetSpecificStoryReturnsStoryForOthers() {
        // given
        String id = "1234";
        String jwt = "jwt";
        StoryEntity story = TestDataUtil.createStoryEntity();
        story.setUserId(UUID.randomUUID());
        story.setAvailable(true);
        // when
        when(repository.findById(id)).thenReturn(Optional.of(story));
        when(jwtUtil.extractId(jwt)).thenReturn("another user");
        StoryEntity result = service.getSpecificStory(id, jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getImage(), story.getImage())
        );
        verify(repository, times(1)).findById(id);
    }

    @Test
    public void testThatCreateStoryWorksWell() {
        // given
        UUID userId = UUID.randomUUID();
        StoryEntity expected = TestDataUtil.createStoryEntity();
        expected.setAvailable(true);
        expected.setUserId(userId);
        StoryEntity story = TestDataUtil.createStoryEntity();
        String jwt = "jwt";
        // when
        when(repository.save(expected)).thenReturn(story);
        when(jwtUtil.extractId(jwt)).thenReturn(userId.toString());
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
