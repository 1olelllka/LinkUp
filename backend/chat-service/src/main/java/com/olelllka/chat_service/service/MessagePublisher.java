package com.olelllka.chat_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.chat_service.configuration.RabbitMQConfig;
import com.olelllka.chat_service.domain.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void createChatNotification(NotificationDto dto) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        rabbitTemplate.convertAndSend(RabbitMQConfig.notification_exchange, "notifications", objectMapper.writeValueAsString(dto));
    }

}
