package com.olelllka.auth_service.service;

import com.olelllka.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageListener {

    private final UserRepository userRepository;
    private static final String deleteQueue = "delete_profile_queue_auth";

    @RabbitListener(queues = deleteQueue)
    public void handleDeleteProfile(UUID profileId) {
        userRepository.deleteById(profileId);
    }
}
