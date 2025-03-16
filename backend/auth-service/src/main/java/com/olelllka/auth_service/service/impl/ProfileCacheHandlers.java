package com.olelllka.auth_service.service.impl;

import com.olelllka.auth_service.domain.dto.PatchUserDto;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.NotFoundException;
import com.olelllka.auth_service.service.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProfileCacheHandlers {

    private final UserRepository userRepository;
    private final MessagePublisher messagePublisher;

    @Cacheable(value = "auth", keyGenerator = "keyGenerator")
    public UserEntity getUserById(String id) {
        return userRepository.findById(UUID.fromString(id)).orElseThrow(() -> new NotFoundException("User with such email was not found."));
    }

    @CachePut(value = "auth", keyGenerator = "keyGenerator")
    public UserEntity patchUserById(String id, PatchUserDto patchUserDto) {
        return userRepository.findById(UUID.fromString(id)).map(user -> {
            Optional.ofNullable(patchUserDto.getEmail()).ifPresent(user::setEmail);
            Optional.ofNullable(patchUserDto.getAlias()).ifPresent(user::setAlias);
            UserEntity saved = userRepository.save(user);
            UserMessageDto dtoToSend = UserMessageDto.builder()
                    .profileId(saved.getUserId())
                    .email(saved.getEmail())
                    .username(saved.getAlias()).build();
            messagePublisher.sendUpdateUserMessage(dtoToSend);
            return saved;
        }).orElseThrow(() -> new NotFoundException("User with such email was not found."));
    }
}
