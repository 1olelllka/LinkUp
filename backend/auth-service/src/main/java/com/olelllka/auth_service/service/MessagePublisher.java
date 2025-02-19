package com.olelllka.auth_service.service;

import com.olelllka.auth_service.config.RabbitMQConfig;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendCreateUserMessage(RegisterUserDto userDto) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.create_user_exchange, "create.user");
    }
}
