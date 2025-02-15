package com.olelllka.notification_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.notification_service.domain.dto.NotificationDto;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageListener {

    private final NotificationRepository repository;
    private final WebSocketHandler webSocketHandler;
    public static final String notification_queue = "notification_queue";
    public static final String delete_profile_queue = "delete_profile_queue_notification";

    @RabbitListener(id="notification", queues = notification_queue)
    public void createNotification(String notification) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        NotificationDto dto = objectMapper.readValue(notification, NotificationDto.class);
        NotificationEntity entity = NotificationEntity.builder()
                .id(dto.getId())
                .text(dto.getText())
                .read(dto.getRead())
                .createdAt(dto.getCreatedAt())
                .userId(UUID.fromString(dto.getUserId()))
                .build();
        repository.save(entity);
        webSocketHandler.sendNotificationToUser(entity.getUserId(), entity.getText());
    }

    @RabbitListener(queues = delete_profile_queue)
    public void handleProfileDeletion(UUID profileId) {
        repository.deleteByUserId(profileId);
    }
}
