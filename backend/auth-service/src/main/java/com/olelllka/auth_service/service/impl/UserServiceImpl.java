package com.olelllka.auth_service.service.impl;

import com.olelllka.auth_service.domain.dto.JWTTokenResponse;
import com.olelllka.auth_service.domain.dto.PatchUserDto;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.AuthProvider;
import com.olelllka.auth_service.domain.entity.Role;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.DuplicateException;
import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import com.olelllka.auth_service.service.JWTUtil;
import com.olelllka.auth_service.service.MessagePublisher;
import com.olelllka.auth_service.service.UserService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MessagePublisher messagePublisher;
    private final PasswordEncoder passwordEncoder;
    private final ProfileCacheHandlers profileCacheHandlers;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public UserEntity registerUser(RegisterUserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail()) || userRepository.existsByAlias(userDto.getAlias())) {
            throw new DuplicateException("User with such credentials already exists.");
        }
        UUID profileId = UUID.randomUUID();
        UserEntity entity = UserEntity.builder()
                .email(userDto.getEmail())
                .userId(profileId)
                .alias(userDto.getAlias())
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
                        .username(userDto.getAlias())
                        .email(userDto.getEmail())
                        .build();
        messagePublisher.sendCreateUserMessage(dtoToSend);
        return saved;
    }

    @Override
    public UserEntity patchUser(String jwt, PatchUserDto patchUserDto) {
        try {
            String id = jwtUtil.extractId(jwt);
            return profileCacheHandlers.patchUserById(id, patchUserDto);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException(ex.getMessage());
        }
    }

    @Override
    public UserEntity getUserByJwt(String jwt) {
        try {
            String id = jwtUtil.extractId(jwt);
            return profileCacheHandlers.getUserById(id);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException(ex.getMessage());
        }
    }

    @Override
    public JWTTokenResponse generateJWTViaEmail(String email) {
        UserEntity user = userRepository.findByEmail(email).get(); // it'll be checked before this function, so it's redundant to create multiple exceptions to "notfounduser"
        String refreshToken = jwtUtil.generateRefreshJWT(user.getUserId());
        redisTemplate.opsForValue().set("refresh_token:" + refreshToken, "", Duration.of(1, ChronoUnit.DAYS));
        return JWTTokenResponse.builder()
                .accessToken(jwtUtil.generateAccessJWT(user.getUserId()))
                .refreshToken(refreshToken)
                .build();
    }
}
