package com.olelllka.auth_service.service;

import com.olelllka.auth_service.domain.dto.JWTTokenResponse;
import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import com.olelllka.auth_service.service.impl.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private final String rawToken = "some.refresh.token";
    private final String redisKey = "refresh_token:" + rawToken;

    @Test
    void refreshToken_shouldThrowUnauthorized_whenKeyNotPresentInRedis() {
        when(redisTemplate.hasKey(redisKey)).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> refreshTokenService.refreshToken(rawToken));

        assertEquals("You are unauthorized to perform such action.", ex.getMessage());
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void refreshToken_shouldReturnNewTokens_whenTokenIsValidAndNotExpired() {
        UUID userId = UUID.randomUUID();
        Claims claims = mock(Claims.class);
        String newRefreshToken = "new.refresh.token";
        String newAccessToken = "new.access.token";

        when(redisTemplate.hasKey(redisKey)).thenReturn(true);
        when(jwtUtil.getClaims(rawToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn(userId.toString());
        when(jwtUtil.generateRefreshJWT(userId)).thenReturn(newRefreshToken);
        when(jwtUtil.generateAccessJWT(userId)).thenReturn(newAccessToken);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Optional<JWTTokenResponse> result = refreshTokenService.refreshToken(rawToken);

        assertTrue(result.isPresent());
        assertEquals(newAccessToken, result.get().getAccessToken());
        assertEquals(newRefreshToken, result.get().getRefreshToken());

        verify(redisTemplate).delete(redisKey);
        verify(valueOperations).set(
                eq("refresh_token:" + newRefreshToken),
                eq(""),
                eq(Duration.of(1, ChronoUnit.DAYS)));
    }

    @Test
    void refreshToken_shouldThrowUnauthorized_whenJwtUtilThrowsJwtException() {
        when(redisTemplate.hasKey(redisKey)).thenReturn(true);
        when(jwtUtil.getClaims(rawToken)).thenThrow(new SignatureException("bad signature"));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> refreshTokenService.refreshToken(rawToken));

        assertEquals("bad signature", ex.getMessage());
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void refreshToken_shouldThrowUnauthorized_whenSubjectIsNotValidUUID() {
        Claims claims = mock(Claims.class);
        when(redisTemplate.hasKey(redisKey)).thenReturn(true);
        when(jwtUtil.getClaims(rawToken)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("not-a-valid-uuid");

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.refreshToken(rawToken));

        verify(redisTemplate, never()).delete(anyString());
    }
}