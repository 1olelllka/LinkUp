package com.olelllka.profile_service.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.olelllka.profile_service.domain.dto.NotificationDto;
import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.olelllka.profile_service.rest.exception.NotFoundException;
import com.olelllka.profile_service.rest.exception.ValidationException;
import com.olelllka.profile_service.service.MessagePublisher;
import com.olelllka.profile_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.elasticsearch.ElasticsearchRestClientHealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository repository;
    private final ProfileDocumentRepository elasticRepository;
    private final MessagePublisher messagePublisher;
    private final ElasticsearchRestClientHealthIndicator elasticHealth;


    @Override
    @Cacheable(value = "profile", keyGenerator = "sha256KeyGenerator")
    public ProfileEntity getProfileById(UUID profileId) {
        return repository.findByIdWithRelationships(profileId).orElseThrow(() -> new NotFoundException("Profile with such id was not found."));
    }

    @Override
    @Transactional
    @CachePut(value = "profile", keyGenerator = "sha256KeyGenerator")
    public ProfileEntity updateProfile(UUID profileId, PatchProfileDto dto) {
        if (!repository.existsById(profileId)) {
            throw new NotFoundException("Profile with such id does not exist");
        }
        ProfileEntity updated = repository.updateProfile(profileId,
                null,
                dto.getName(),
                null,
                dto.getGender() != null ? dto.getGender().toString() : null,
                dto.getPhoto(),
                dto.getAboutMe(),
                dto.getDateOfBirth()
        );
        ProfileDocumentDto documentDto = ProfileDocumentDto.builder()
                .id(updated.getId())
                .name(updated.getName())
                .photo(updated.getPhoto())
                .build();
        messagePublisher.updateProfile(documentDto);
        return updated;
    }

    @Override
    @Transactional
    @CacheEvict(value = "profile", keyGenerator = "sha256KeyGenerator")
    public void deleteById(UUID profileId) {
        repository.deleteById(profileId);
        messagePublisher.deleteProfile(profileId);
    }

    @Override
    @Transactional
    public void followNewProfile(UUID profileId, UUID follow) {
        if (profileId.equals(follow)) {
            throw new ValidationException("Follower id and Followee id must not be the same");
        }
        if (!repository.existsById(profileId) || !repository.existsById(follow)) {
            throw new NotFoundException("Profile/s with such id/s is/are not found.");
        }
        if (!repository.isFollowing(profileId, follow)) {
            repository.follow(profileId, follow);
            ProfileEntity follower = repository.findByIdWithRelationships(profileId).get();
            NotificationDto notification = NotificationDto.builder()
                    .read(false)
                    .userId(follow.toString())
                    .createdAt(new Date())
                    .text(follower.getUsername() + " followed your profile.")
                    .build();
            try {
                messagePublisher.createFollowNotification(notification);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            throw new ValidationException("You've already followed this profile.");
        }
    }

    @Override
    public void unfollowProfile(UUID profileId, UUID followId) {
        if (profileId.equals(followId)) {
            throw new ValidationException("Follower id and Followee id must not be the same.");
        }
        if (!repository.existsById(profileId) || !repository.existsById(followId)) {
            throw new NotFoundException("Profile/s with such id/s is/are not found.");
        }
        if (repository.isFollowing(profileId, followId)) {
            repository.unfollow(profileId, followId);
        } else {
            throw new ValidationException("You are not following this profile.");
        }
    }

    @Override
    public Page<ProfileEntity> getFollowersById(UUID profileId, Pageable pageable) {
        return repository.findAllFollowersByProfileId(profileId, pageable);
    }

    @Override
    public Page<ProfileEntity> getFolloweesById(UUID profileId, Pageable pageable) {
        return repository.findAllFolloweeByProfileId(profileId, pageable);
    }

    @Override
    public Page<ProfileEntity> searchForProfile(String search, Pageable pageable) {
        // I'll create two options: elasticsearch search and neo4j search.
        // Elasticsearch's will be main and neo4j's in case of failure of elasticsearch
        if (elasticHealth.getHealth(false).getStatus().equals(Status.UP)) {
            Page<ProfileDocument> documents = elasticRepository.findByParams(search, pageable);
            return documents.map(this::documentToEntity);
        } else {
            return repository.findProfileByParam(search, pageable);
        }
    }

    private ProfileEntity documentToEntity(ProfileDocument document) {
        return ProfileEntity.builder()
                .id(document.getId())
                .username(document.getUsername())
                .photo(document.getPhoto())
                .name(document.getName())
                .email(document.getEmail())
                .build();
    }
}
