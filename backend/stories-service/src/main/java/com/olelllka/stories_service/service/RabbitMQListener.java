package com.olelllka.stories_service.service;

import com.olelllka.stories_service.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RabbitMQListener {

    private final String deleteProfileQueue = "delete_profile_queue";
    private final StoryRepository storyRepository;

    @RabbitListener(queues = deleteProfileQueue)
    public void deleteStoriesForSpecificProfile(UUID profileId) {
        storyRepository.deleteByUserId(profileId);
    }

}
