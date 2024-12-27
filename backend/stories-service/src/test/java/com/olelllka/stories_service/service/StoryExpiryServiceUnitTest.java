package com.olelllka.stories_service.service;

import com.olelllka.stories_service.TestDataUtil;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.repository.StoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoryExpiryServiceUnitTest {

    @Mock
    private StoryRepository repository;
    @InjectMocks
    private StoryExpiryService service;

    @Test
    public void testThatExpiredStoriesWillBeUnavailable() {
        // given
        StoryEntity story = TestDataUtil.createStoryEntity();
        story.setAvailable(true);
        List<StoryEntity> expiredStories = List.of(story);
        story.setAvailable(false);
        List<StoryEntity> expected = List.of(story);
        // when
        when(repository.findByAvailableTrueAndCreatedAtBefore(any(Date.class))).thenReturn(expiredStories);
        service.markExpiredStories();
        // then
        verify(repository, times(1)).findByAvailableTrueAndCreatedAtBefore(any(Date.class));
        verify(repository, times(1)).saveAll(expected);
    }
}
