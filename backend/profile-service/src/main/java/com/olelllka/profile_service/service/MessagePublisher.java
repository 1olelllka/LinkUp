package com.olelllka.profile_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.profile_service.configuration.RabbitMQConfig;
import com.olelllka.profile_service.domain.dto.NotificationDto;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void updateProfile(ProfileDocumentDto dto) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.profile_exchange, "update_profile", dto);
    }

    public void deleteProfile(UUID id) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.profile_fanout_exchange, "", id);
    }

    public void createFollowNotification(NotificationDto notificationDto) throws JsonProcessingException {
        rabbitTemplate.convertAndSend(RabbitMQConfig.notification_exchange, "notifications", objectMapper.writeValueAsString(notificationDto));
    }
}
