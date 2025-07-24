package com.olelllka.auth_service.service;

import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import com.olelllka.auth_service.service.impl.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

    @Mock
    private JWTUtil jwtUtil;
    @InjectMocks
    private RefreshTokenService tokenService;


    @Test
    public void testThatRefreshTokenThrowsExceptions() {
        // given
        String token = "invalid_token";
        // when
        when(jwtUtil.getClaims(token)).thenThrow(JwtException.class);
        // then
        assertThrows(UnauthorizedException.class, () -> tokenService.refreshToken(token));
    }

    @Test
    public void testThatRefreshTokenReturnsEmptyTokenIfTokenExpired() {
        // given
        String token = "expired_token";
        Claims mockClaims = mock(Claims.class);
        // when
        when(jwtUtil.getClaims(token)).thenReturn(mockClaims);
        when(jwtUtil.getClaims(token).getExpiration()).thenReturn(Date.from(Instant.now().minusSeconds(123)));
        when(jwtUtil.getClaims(token).getSubject()).thenReturn("id");
        // then
        Optional<String> result = tokenService.refreshToken(token);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testThatRefreshTokenReturnsValidAccessToken() {
        // given
        String token = "valid_token";
        Claims claims = mock(Claims.class);
        UUID id = UUID.randomUUID();
        // when
        when(jwtUtil.getClaims(token)).thenReturn(claims);
        when(jwtUtil.getClaims(token).getSubject()).thenReturn(id.toString());
        when(jwtUtil.getClaims(token).getExpiration()).thenReturn(Date.from(Instant.now().plusSeconds(1234)));
        when(jwtUtil.generateRefreshJWT(id)).thenReturn("access_token");
        // then
        Optional<String> result = tokenService.refreshToken(token);
        assertTrue(result.isPresent());
        assertEquals("access_token", result.get());
    }
}
