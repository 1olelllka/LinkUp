package com.olelllka.stories_service.repository;

import com.olelllka.stories_service.TestDataUtil;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.*;
import org.testcontainers.containers.MongoDBContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
public class StoryRepositoryMongoDataTest {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0");

    static {
        mongoDBContainer.start();
    }

    @AfterAll
    static void tearDown() {
        mongoDBContainer.stop();
        mongoDBContainer.close();
    }

    @Autowired
    private StoryRepository repository;

    @Test
    public void testThatMongoReturnsPageOfStoriesForSpecificUser() {
        StoryEntity story = TestDataUtil.createStoryEntity();
        UUID id = UUID.randomUUID();
        story.setUserId(id);
        StoryEntity saved = repository.save(story);
        Pageable pageable = PageRequest.of(0, 1);
        Page<StoryEntity> expected = new PageImpl<>(List.of(saved));

        Page<StoryEntity> result = repository.findStoryByUserId(id, pageable);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().get(0).getImage(), expected.getContent().get(0).getImage())
        );
    }

    @Test
    public void testThatMongoReturnsNothingForUserThatDoesNotHaveStory() {
        StoryEntity story = TestDataUtil.createStoryEntity();
        story.setUserId(UUID.randomUUID());
        Pageable pageable = PageRequest.of(0, 1);
        Page<StoryEntity> result = repository.findStoryByUserId(UUID.randomUUID(), pageable);

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

    @Test
    public void testThatMongoDeletesAllOfTheStoriesByUserId() {
        UUID profileId = UUID.randomUUID();
        StoryEntity story1 = TestDataUtil.createStoryEntity();
        story1.setUserId(profileId);
        StoryEntity story2 = TestDataUtil.createStoryEntity();
        story2.setUserId(profileId);
        repository.saveAll(List.of(story1, story2));

        repository.deleteByUserId(profileId);

        Pageable pageable = PageRequest.of(0, 1);
        Page<StoryEntity> stories = repository.findStoryByUserId(profileId, pageable);
        assertEquals(stories.getTotalElements(), 0);
    }

}
