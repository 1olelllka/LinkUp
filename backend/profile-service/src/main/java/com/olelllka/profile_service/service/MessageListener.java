package com.olelllka.profile_service.service;

import com.olelllka.profile_service.configuration.RabbitMQConfig;
import com.olelllka.profile_service.domain.dto.UserMessageDto;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import com.olelllka.profile_service.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MessageListener {

    private final ProfileRepository profileRepository;
    private final ProfileDocumentRepository documentRepository;

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
        profile = profileRepository.save(profile);
        documentRepository.save(ProfileDocument
                .builder()
                .id(profile.getId())
                .photo(null)
                .username(profile.getUsername())
                .name(profile.getUsername())
                .build());
    }

}
