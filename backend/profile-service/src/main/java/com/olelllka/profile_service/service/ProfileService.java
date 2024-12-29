package com.olelllka.profile_service.service;

import com.olelllka.profile_service.domain.dto.ProfileDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;

public interface ProfileService {
    ProfileEntity createProfile(ProfileEntity entity);
}
