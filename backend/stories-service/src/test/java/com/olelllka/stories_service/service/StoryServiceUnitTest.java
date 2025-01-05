package com.olelllka.stories_service.service;

import com.olelllka.stories_service.TestDataUtil;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.repository.StoryRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoryServiceUnitTest {

    @Mock
    private StoryRepository repository;
    @InjectMocks
    private StoryServiceImpl service;

    @Test
    public void testThatGetStoriesForUserReturnsPageOfResults() {
        // given
        UUID id = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 1);
        StoryEntity entity = TestDataUtil.createStoryEntity();
        Page<StoryEntity> expected = new PageImpl<>(List.of(entity));
        // when
        when(repository.findStoryByUserId(id, pageable)).thenReturn(expected);
        Page<StoryEntity> result = service.getStoriesForUser(id, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().get(0).getImage(), expected.getContent().get(0).getImage())
        );
        verify(repository, times(1)).findStoryByUserId(id, pageable);
    }

    @Test
    public void testThatGetSpecificStoryThrowsNotFoundException() {
        // given
        String id = "1234";
        // when
        when(repository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.getSpecificStory(id));
    }

    @Test
    public void testThatGetSpecificStoryReturnsStory() {
        // given
        String id = "1234";
        StoryEntity story = TestDataUtil.createStoryEntity();
        // when
        when(repository.findById(id)).thenReturn(Optional.of(story));
        StoryEntity result = service.getSpecificStory(id);
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
        // when
        when(repository.save(expected)).thenReturn(story);
        StoryEntity result = service.createStory(userId, story);
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
    public void testThatUpdateStoryThrowsNotFoundException() {
        // given
        String storyId = "1234";
        StoryEntity entity = TestDataUtil.createStoryEntity();
        // when
        when(repository.findById(storyId)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.updateSpecificStory(storyId, entity));
        verify(repository, never()).save(any(StoryEntity.class));
    }

    @Test
    public void testThatUpdateStoryReturnsUpdatedStory() {
        // given
        String storyId = "1234";
        StoryEntity updated = TestDataUtil.createStoryEntity();
        updated.setImage("UPDATED");
        StoryEntity expected = TestDataUtil.createStoryEntity();
        expected.setImage(updated.getImage());
        expected.setAvailable(true);
        // when
        when(repository.findById(storyId)).thenReturn(Optional.of(TestDataUtil.createStoryEntity()));
        when(repository.save(any(StoryEntity.class))).thenReturn(expected); // if I put expected in save it'll throw error 'coz of date
        StoryEntity result = service.updateSpecificStory(storyId, updated);
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
    public void testThatDeleteStoryWorksWell() {
        // given
        String id = "1234";
        // when
        service.deleteSpecificStory(id);
        // then
        verify(repository, times(1)).deleteById(id);
    }
}
