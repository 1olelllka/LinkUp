package com.olelllka.stories_service.repository;

import com.olelllka.stories_service.TestDataUtil;
import com.olelllka.stories_service.TestcontainersConfiguration;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.testcontainers.containers.MongoDBContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Import(TestcontainersConfiguration.class)
public class StoryRepositoryMongoDataTest {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0");

    static {
        mongoDBContainer.start();
    }

    @Autowired
    private StoryRepository repository;

    @Test
    public void testThatMongoReturnsPageOfStoriesForSpecificUser() {
        StoryEntity story = TestDataUtil.createStoryEntity();
        story.setUserId("1234");
        StoryEntity saved = repository.save(story);
        Pageable pageable = PageRequest.of(0, 1);
        Page<StoryEntity> expected = new PageImpl<>(List.of(saved));

        Page<StoryEntity> result = repository.findStoryByUserId("1234", pageable);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().get(0).getImage(), expected.getContent().get(0).getImage())
        );
    }

    @Test
    public void testThatMongoReturnsNothingForUserThatDoesNotHaveStory() {
        StoryEntity story = TestDataUtil.createStoryEntity();
        story.setUserId("1234");
        Pageable pageable = PageRequest.of(0, 1);
        Page<StoryEntity> result = repository.findStoryByUserId("12345", pageable);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().size(), 0)
        );
    }

    @Test
    public void testThatMongoReturnsListOfExpiredStories() {
        StoryEntity story = TestDataUtil.createStoryEntity();
        story.setAvailable(true);
        repository.save(story);
        List<StoryEntity> stories = repository.findByAvailableTrueAndCreatedAtBefore(new Date());
        assertEquals(stories.size(), 1);
    }

    @Test
    public void testThatMongoReturnsEmptyListOfStories() {
        StoryEntity story = TestDataUtil.createStoryEntity();
        story.setAvailable(true);
        repository.save(story);
        List<StoryEntity> stories = repository.findByAvailableTrueAndCreatedAtBefore(Date.from(Instant.now().minus(24, ChronoUnit.HOURS)));
        assertEquals(stories.size(), 0);
    }

}
