package com.olelllka.profile_service.service;

import com.olelllka.profile_service.configuration.RabbitMQConfig;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessageListener {

    @Autowired
    private ProfileDocumentRepository documentRepository;

    @RabbitListener(queues = RabbitMQConfig.create_update_queue, id = "cu-profile")
    public void createUpdateProfileOnElasticsearch(ProfileDocumentDto dto) {
        ProfileDocument document = ProfileDocument.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .photo(dto.getPhoto())
                .build();
        documentRepository.save(document);
    }

    @RabbitListener(queues = RabbitMQConfig.delete_queue, id="d-profile")
    public void deleteProfileOnElasticSearch(UUID id) {
        documentRepository.deleteById(id);
    }
}
