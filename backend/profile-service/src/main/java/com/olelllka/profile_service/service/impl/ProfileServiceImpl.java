package com.olelllka.profile_service.service.impl;

import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.olelllka.profile_service.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository repository;

    @Override
    public ProfileEntity createProfile(ProfileEntity entity) {
        entity.setCreatedAt(LocalDate.now());
        return repository.save(entity);
    }
}
