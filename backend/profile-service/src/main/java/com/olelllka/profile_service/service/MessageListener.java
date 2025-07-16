package com.olelllka.profile_service.service;

import com.olelllka.profile_service.configuration.RabbitMQConfig;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import com.olelllka.profile_service.domain.dto.UserMessageDto;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.olelllka.profile_service.rest.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageListener {

    private final ProfileDocumentRepository documentRepository;
    private final ProfileRepository profileRepository;

    @RabbitListener(queues = RabbitMQConfig.create_user_queue)
    @Transactional
    public void createProfileFromAuthService(UserMessageDto messageDto) {
        ProfileEntity profile = ProfileEntity.builder()
                .id(messageDto.getProfileId())
                .gender(messageDto.getGender())
                .createdAt(LocalDate.now())
                .email(messageDto.getEmail())
                .name(messageDto.getName())
                .username(messageDto.getUsername())
                .dateOfBirth(messageDto.getDateOfBirth())
                .build();
        profileRepository.save(profile);
        ProfileDocument document = ProfileDocument.builder()
                .email(messageDto.getEmail())
                .name(messageDto.getName())
                .username(messageDto.getUsername())
                .id(messageDto.getProfileId())
                .photo("")
                .build();
        documentRepository.save(document);
    }

    @RabbitListener(queues = RabbitMQConfig.update_user_queue)
    @Transactional
    public void updateProfileFromAuthService(UserMessageDto messageDto) {
        profileRepository.updateProfile(messageDto.getProfileId(),
                                        messageDto.getUsername(),
                                        null,
                                        messageDto.getEmail(),
                                        null,
                                    null,
                                null,
                                null);
        ProfileDocument document = documentRepository.findById(messageDto.getProfileId())
                .orElseThrow(() -> new NotFoundException("Document was not found."));
        document.setUsername(messageDto.getUsername());
        document.setEmail(messageDto.getEmail());
        documentRepository.save(document);
    }

    @RabbitListener(queues = RabbitMQConfig.update_elastic_queue, id = "u-profile")
    public void updateProfileOnElasticsearch(ProfileDocumentDto dto) {
        ProfileDocument document = ProfileDocument.builder()
                .id(dto.getId())
                .name(dto.getName())
                .photo(dto.getPhoto())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .build();
        documentRepository.save(document);
    }

    @RabbitListener(queues = RabbitMQConfig.delete_queue_elastic, id="d-profile")
    public void deleteProfileOnElasticSearch(UUID id) {
        documentRepository.deleteById(id);
    }
}
