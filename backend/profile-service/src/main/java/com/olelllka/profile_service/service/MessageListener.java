package com.olelllka.profile_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.olelllka.profile_service.configuration.RabbitMQConfig;
import com.olelllka.profile_service.domain.dto.UserMessageDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MessageListener {

    private final ProfileRepository profileRepository;
    private final RedisTemplate<String, ProfileEntity> redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.create_user_queue)
    @Transactional
    public void createProfileFromAuthService(UserMessageDto messageDto) {
        ProfileEntity profile = ProfileEntity.builder()
                .id(messageDto.getProfileId())
                .gender(messageDto.getGender())
                .createdAt(LocalDate.now())
                .name(messageDto.getName())
                .username(messageDto.getUsername())
                .dateOfBirth(messageDto.getDateOfBirth())
                .build();
        profileRepository.save(profile);
    }

    @RabbitListener(queues = RabbitMQConfig.update_user_queue)
    @Transactional
    public void updateProfileFromAuthService(UserMessageDto messageDto) throws JsonProcessingException {
        ProfileEntity newProfile = profileRepository.updateProfile(messageDto.getProfileId(),
                                        messageDto.getUsername(),
                                        null,
                                        messageDto.getEmail(),
                                        null,
                                    null,
                                null,
                                null);
        redisTemplate.opsForValue().set("profile::" + SHA256.hash(messageDto.getProfileId().toString()),
                newProfile, 60, TimeUnit.MINUTES);
    }
}
