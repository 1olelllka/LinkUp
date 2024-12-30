package com.olelllka.profile_service.service;

import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.dto.ProfileDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;

import java.util.UUID;

public interface ProfileService {
    ProfileEntity createProfile(ProfileEntity entity);

    ProfileEntity getProfileById(UUID profileId);

    ProfileEntity updateProfile(UUID profileId, PatchProfileDto dto);
}
