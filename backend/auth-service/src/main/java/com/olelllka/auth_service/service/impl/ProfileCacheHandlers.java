package com.olelllka.auth_service.service.impl;

import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfileCacheHandlers {

    private final UserRepository userRepository;

    @Cacheable(value = "auth", keyGenerator = "keyGenerator")
    public UserEntity getUserById(String id) {
        return userRepository.findById(UUID.fromString(id)).orElseThrow(() -> new NotFoundException("User with such email was not found."));
    }
}
