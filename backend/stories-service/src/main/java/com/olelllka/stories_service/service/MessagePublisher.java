package com.olelllka.stories_service.service;

import com.olelllka.stories_service.domain.dto.StoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.olelllka.stories_service.configuration.RabbitMQConfig.CREATE_QUEUE_EXCHANGE;

@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendCreatedStory(StoryDto storyDto) {
        rabbitTemplate.convertAndSend(CREATE_QUEUE_EXCHANGE, "story.create", storyDto);
    }

}
