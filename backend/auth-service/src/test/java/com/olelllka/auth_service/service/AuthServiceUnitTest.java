package com.olelllka.auth_service.service;

import com.olelllka.auth_service.domain.dto.JWTTokenResponse;
import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import com.olelllka.auth_service.service.impl.RefreshTokenService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @InjectMocks
    private RefreshTokenService tokenService;


    @Test
    public void testThatRefreshTokenThrowsExceptions() {
        // given
        String token = "invalid_token";
        // when
        when(redisTemplate.hasKey("refresh_token:" + token)).thenReturn(false);
        // then
        assertThrows(UnauthorizedException.class, () -> tokenService.refreshToken(token));
    }

    @Test
    public void testThatRefreshTokenThrowsExceptionIfTokenExpired() {
        // given
        String token = "expired_token";
        Claims mockClaims = mock(Claims.class);
        // when
        when(redisTemplate.hasKey("refresh_token:" + token)).thenReturn(true);
        when(jwtUtil.getClaims(token)).thenReturn(mockClaims);
        when(jwtUtil.getClaims(token).getExpiration()).thenReturn(Date.from(Instant.now().minusSeconds(123)));
        when(jwtUtil.getClaims(token).getSubject()).thenReturn("id");
        // then
        assertThrows(UnauthorizedException.class, () -> tokenService.refreshToken(token));
    }

    @Test
    public void testThatRefreshTokenReturnsValidAccessToken() {
        // given
        String token = "valid_token";
        Claims claims = mock(Claims.class);
        UUID id = UUID.randomUUID();
        // when
        when(redisTemplate.hasKey("refresh_token:"+token)).thenReturn(true);
        when(jwtUtil.getClaims(token)).thenReturn(claims);
        when(jwtUtil.getClaims(token).getSubject()).thenReturn(id.toString());
        when(jwtUtil.getClaims(token).getExpiration()).thenReturn(Date.from(Instant.now().plusSeconds(1234)));
        when(jwtUtil.generateAccessJWT(id)).thenReturn("access_token");
        when(jwtUtil.generateRefreshJWT(id)).thenReturn("refresh_token");
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        // then
        Optional<JWTTokenResponse> result = tokenService.refreshToken(token);
        assertTrue(result.isPresent());
        assertEquals("access_token", result.get().getAccessToken());
        assertEquals("refresh_token", result.get().getRefreshToken());
        verify(redisTemplate.opsForValue(), times(1)).set("refresh_token:refresh_token", "", Duration.of(1, ChronoUnit.DAYS));
    }
}
