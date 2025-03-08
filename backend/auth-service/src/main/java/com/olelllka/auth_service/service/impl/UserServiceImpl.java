package com.olelllka.auth_service.service.impl;

import com.olelllka.auth_service.domain.dto.JWTToken;
import com.olelllka.auth_service.domain.dto.PatchUserDto;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.AuthProvider;
import com.olelllka.auth_service.domain.entity.Role;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.DuplicateException;
import com.olelllka.auth_service.rest.exception.NotFoundException;
import com.olelllka.auth_service.service.JWTUtil;
import com.olelllka.auth_service.service.MessagePublisher;
import com.olelllka.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MessagePublisher messagePublisher;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    @Override
    public UserEntity registerUser(RegisterUserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail()) || userRepository.existsByUsername(userDto.getUsername())) {
            throw new DuplicateException("User with such credentials already exists.");
        }
        UUID profileId = UUID.randomUUID();
        UserEntity entity = UserEntity.builder()
                .email(userDto.getEmail())
                .userId(profileId)
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .providerId("")
                .build();
        UserEntity saved = userRepository.save(entity);
        UserMessageDto dtoToSend = UserMessageDto.builder()
                        .profileId(profileId)
                        .dateOfBirth(userDto.getDateOfBirth())
                        .name(userDto.getName())
                        .gender(userDto.getGender())
                        .username(userDto.getUsername())
                        .email(userDto.getEmail())
                        .build();
        messagePublisher.sendCreateUserMessage(dtoToSend);
        return saved;
    }

    @Override
    public UserEntity patchUser(String jwt, PatchUserDto patchUserDto) {
        String email = jwtUtil.extractUsername(jwt);
        return userRepository.findByEmail(email).map(user -> {
            Optional.ofNullable(patchUserDto.getEmail()).ifPresent(user::setEmail);
            Optional.ofNullable(patchUserDto.getUsername()).ifPresent(user::setUsername);
            UserEntity saved = userRepository.save(user);
            UserMessageDto dtoToSend = UserMessageDto.builder()
                    .profileId(saved.getUserId())
                    .email(saved.getEmail())
                    .username(saved.getUsername()).build();
            messagePublisher.sendUpdateUserMessage(dtoToSend);
            return saved;
        }).orElseThrow(() -> new NotFoundException("User with such email was not found."));
    }

    @Override
    public UserEntity getUserByJwt(String jwt) {
        String email = jwtUtil.extractUsername(jwt);
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User with such email was not found."));
    }
}
