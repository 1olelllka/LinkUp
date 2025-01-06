package com.olelllka.profile_service.service;

import com.olelllka.profile_service.configuration.RabbitMQConfig;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessagePublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void createUpdateProfile(ProfileDocumentDto dto) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.profile_exchange, "create_and_update_profile", dto);
    }

    public void deleteProfile(UUID id) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.profile_exchange, "delete_profile", id);
    }
}
