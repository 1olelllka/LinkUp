package com.olelllka.auth_service.service;

import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.OAuthIdentityRepository;
import com.olelllka.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageListener {

    private final UserRepository userRepository;
    private final OAuthIdentityRepository identityRepository;
    private static final String deleteQueue = "delete_profile_queue_auth";
    private final RedisTemplate<String, String> redisTemplate;

    @RabbitListener(queues = deleteQueue)
    public void handleDeleteProfile(UUID profileId) {
        Optional<UserEntity> user = userRepository.findById(profileId);
        user.ifPresent(userEntity -> redisTemplate.delete("auth::" + SHA256.hash(userEntity.getEmail())));
        userRepository.deleteById(profileId);
        identityRepository.deleteById(profileId);
    }
}
