package com.olelllka.profile_service.service.impl;

import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.olelllka.profile_service.rest.exception.NotFoundException;
import com.olelllka.profile_service.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository repository;

    @Override
    public ProfileEntity createProfile(ProfileEntity entity) {
        entity.setCreatedAt(LocalDate.now());
        return repository.save(entity);
    }

    @Override
    public ProfileEntity getProfileById(UUID profileId) {
        return repository.findById(profileId).orElseThrow(() -> new NotFoundException("Profile with such id was not found."));
    }

    @Override
    public ProfileEntity updateProfile(UUID profileId, PatchProfileDto dto) {
        return repository.findById(profileId).map(profile -> {
            Optional.ofNullable(dto.getUsername()).ifPresent(profile::setUsername);
            Optional.ofNullable(dto.getName()).ifPresent(profile::setName);
            Optional.ofNullable(dto.getEmail()).ifPresent(profile::setEmail);
            Optional.ofNullable(dto.getGender()).ifPresent(profile::setGender);
            Optional.ofNullable(dto.getPhoto()).ifPresent(profile::setPhoto);
            Optional.ofNullable(dto.getAboutMe()).ifPresent(profile::setAboutMe);
            Optional.ofNullable(dto.getDateOfBirth()).ifPresent(profile::setDateOfBirth);
            return repository.save(profile);
        }).orElseThrow(() -> new NotFoundException("Profile with such id was not found."));
    }
}
