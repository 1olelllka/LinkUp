package com.olelllka.stories_service.service;

import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.repository.StoryRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
@Log
public class StoryExpiryService {

    @Autowired
    private StoryRepository repository;

    @Scheduled(fixedRate = 3600 * 1000) // 1 hour
    public void markExpiredStories() {
        Date expiryDate = Date.from(Instant.now().minus(24, ChronoUnit.HOURS));
        List<StoryEntity> expiredStories = repository.findByAvailableTrueAndCreatedAtBefore(expiryDate);
        expiredStories.forEach(story -> story.setAvailable(false));
        repository.saveAll(expiredStories);
    }
}
