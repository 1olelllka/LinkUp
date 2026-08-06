package com.olelllka.auth_service.service;

import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.domain.dto.JWTTokenResponse;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.feign.ProfileClient;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.DuplicateException;
import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import com.olelllka.auth_service.service.impl.ProfileCacheHandlers;
import com.olelllka.auth_service.service.impl.UserServiceImpl;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MessagePublisher messagePublisher;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private ProfileCacheHandlers profileCacheHandlers;
    @Mock
    private ProfileClient profileClient;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void testThatRegisterUserThrowsExceptionIfDuplicateEmail() {
        // given
        RegisterUserDto registerUserDto = TestDataUtil.createRegisterUserDto();
        // when
        when(userRepository.existsByEmail(registerUserDto.getEmail())).thenReturn(true);
        // then
        assertThrows(DuplicateException.class, () -> userService.registerUser(registerUserDto));
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(messagePublisher, never()).sendCreateUserMessage(any(UserMessageDto.class));
        verify(profileClient, never()).getUsernameAvailability(anyString());
    }

    @Test
    void testThatRegisterUserThrowsExceptionIfDuplicateUsername() {
        // given
        RegisterUserDto registerUserDto = TestDataUtil.createRegisterUserDto();
        // when
        when(userRepository.existsByEmail(registerUserDto.getEmail())).thenReturn(false);
        when(profileClient.getUsernameAvailability(registerUserDto.getAlias())).thenReturn(ResponseEntity.status(200).build());
        // then
        assertThrows(DuplicateException.class, () -> userService.registerUser(registerUserDto));
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(messagePublisher, never()).sendCreateUserMessage(any(UserMessageDto.class));
    }

    @Test
    void testThatRegisterUserThrowsExceptionIfServerError() {
        // given
        RegisterUserDto registerUserDto = TestDataUtil.createRegisterUserDto();
        // when
        when(userRepository.existsByEmail(registerUserDto.getEmail())).thenReturn(false);
        when(profileClient.getUsernameAvailability(registerUserDto.getAlias())).thenReturn(ResponseEntity.status(500).build());
        // then
        assertThrows(RuntimeException.class, () -> userService.registerUser(registerUserDto));
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(messagePublisher, never()).sendCreateUserMessage(any(UserMessageDto.class));
    }

    @Test
    void testThatRegisterUserReturnsNewUserWithSentMessageToQueue() {
        // given
        RegisterUserDto registerUserDto = TestDataUtil.createRegisterUserDto();
        // when
        when(userRepository.existsByEmail(registerUserDto.getEmail())).thenReturn(false);
        when(profileClient.getUsernameAvailability(registerUserDto.getAlias())).thenReturn(ResponseEntity.status(404).build());
        when(passwordEncoder.encode(registerUserDto.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArgument(0));
        // then
        UserEntity saved = userService.registerUser(registerUserDto);
        assertNotNull(saved);
        assertEquals("encoded", saved.getPassword());
        assertNotNull(saved.getUserId());
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(messagePublisher, times(1)).sendCreateUserMessage(any(UserMessageDto.class));
    }


    @Test
    void testThatGetUserByJWTWorksFine() {
        // given
        String jwt = "jwt";
        UserEntity expected = TestDataUtil.createUserEntity();
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(expected.getEmail());
        when(profileCacheHandlers.getUserById(expected.getEmail())).thenReturn(expected);
        UserEntity result = userService.getUserByJwt(jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getUserId(), expected.getUserId())
        );
    }

    @Test
    void testThatGetUserByJWTThrowsUnauthorizedException() {
        // given
        String jwt = "invalid";
        // when
        when(jwtUtil.extractId(jwt)).thenThrow(JwtException.class);
        assertThrows(UnauthorizedException.class, () -> userService.getUserByJwt(jwt));
        // then
        verify(profileCacheHandlers, never()).getUserById(anyString());
    }

    @Test
    void testThatGenerateJWTFromEmailWorksFine() {
        UUID userId = UUID.randomUUID();
        UserEntity user = TestDataUtil.createUserEntity();
        user.setUserId(userId);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateRefreshJWT(userId)).thenReturn("refreshToken");
        when(jwtUtil.generateAccessJWT(userId)).thenReturn("accessToken");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        JWTTokenResponse response = userService.generateJWTViaEmail(user.getEmail());
        assertNotNull(response);
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("accessToken", response.getAccessToken());
        verify(valueOperations, times(1)).set("refresh_token:refreshToken", "", Duration.of(1, ChronoUnit.DAYS));
    }

}
