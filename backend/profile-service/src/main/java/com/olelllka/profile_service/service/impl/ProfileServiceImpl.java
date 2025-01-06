package com.olelllka.profile_service.service.impl;

import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.olelllka.profile_service.rest.exception.NotFoundException;
import com.olelllka.profile_service.rest.exception.ValidationException;
import com.olelllka.profile_service.service.MessagePublisher;
import com.olelllka.profile_service.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository repository;

    @Autowired
    private MessagePublisher messagePublisher;

    @Override
    @Transactional
    public ProfileEntity createProfile(ProfileEntity entity) {
        entity.setCreatedAt(LocalDate.now());
        ProfileEntity saved = repository.save(entity);
        ProfileDocumentDto dto = ProfileDocumentDto.builder()
                        .id(saved.getId())
                        .name(saved.getName())
                        .username(saved.getUsername())
                        .email(saved.getEmail())
                        .build();
        messagePublisher.createUpdateProfile(dto);
        return saved;
    }

    @Override
    public ProfileEntity getProfileById(UUID profileId) {
        return repository.findByIdWithRelationships(profileId).orElseThrow(() -> new NotFoundException("Profile with such id was not found."));
    }

    @Override
    @Transactional
    public ProfileEntity updateProfile(UUID profileId, PatchProfileDto dto) {
        if (!repository.existsById(profileId)) {
            throw new NotFoundException("Profile with such id does not exist");
        }
        ProfileEntity updated = repository.updateProfile(profileId,
                dto.getUsername(),
                dto.getName(),
                dto.getEmail(),
                dto.getGender() != null ? dto.getGender().toString() : null,
                dto.getPhoto(),
                dto.getAboutMe(),
                dto.getDateOfBirth());
        ProfileDocumentDto documentDto = ProfileDocumentDto.builder()
                .id(updated.getId())
                .email(updated.getEmail())
                .name(updated.getName())
                .username(updated.getUsername())
                .build();
        messagePublisher.createUpdateProfile(documentDto);
        return updated;
    }

    @Override
    @Transactional
    public void deleteById(UUID profileId) {
        repository.deleteById(profileId);
        messagePublisher.deleteProfile(profileId);
    }

    @Override
    public void followNewProfile(UUID profileId, UUID follow) {
        if (profileId.equals(follow)) {
            throw new ValidationException("Follower id and Followee id must not be the same");
        }
        if (!repository.existsById(profileId) || !repository.existsById(follow)) {
            throw new NotFoundException("Profile/s with such id/s is/are not found.");
        }
        if (!repository.isFollowing(profileId, follow)) {
            repository.follow(profileId, follow);
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
        // TODO: Create elasticsearch implementation

        // neo4j implementation (it'll be far less performant than elasticsearch)
        return repository.findProfileByParam(search, pageable);
    }
}
