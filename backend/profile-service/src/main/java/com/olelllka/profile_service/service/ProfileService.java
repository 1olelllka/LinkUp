package com.olelllka.profile_service.service;

import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProfileService {
    ProfileEntity getProfileById(UUID profileId);

    ProfileEntity updateProfile(UUID profileId, PatchProfileDto dto, String jwt);

    void deleteById(UUID profileId, String jwt);

    void followNewProfile(UUID profileId, UUID profileId1, String jwt);

    void unfollowProfile(UUID profileId, UUID followId, String jwt);

    Page<ProfileEntity> getFollowersById(UUID profileId, Pageable pageable);

    Page<ProfileEntity> getFolloweesById(UUID profileId, Pageable pageable);

    Page<ProfileEntity> searchForProfile(String search, Pageable pageable);
}
