package com.olelllka.auth_service.service.impl;

import com.olelllka.auth_service.domain.dto.JWTTokenResponse;
import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import com.olelllka.auth_service.service.AuthService;
import com.olelllka.auth_service.service.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements AuthService {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Optional<JWTTokenResponse> refreshToken(String token) {
        if (!Boolean.TRUE.equals(redisTemplate.hasKey("refresh_token:" + token))) {
            throw new UnauthorizedException("You are unauthorized to perform such action.");
        }

        try {
            Claims claims = jwtUtil.getClaims(token);
            UUID userId = UUID.fromString(claims.getSubject());

            String refreshToken = jwtUtil.generateRefreshJWT(userId);

            redisTemplate.delete("refresh_token:" + token);
            redisTemplate.opsForValue().set(
                    "refresh_token:" + refreshToken,
                    "",
                    Duration.ofDays(1)
            );

            return Optional.of(
                    JWTTokenResponse.builder()
                            .accessToken(jwtUtil.generateAccessJWT(userId))
                            .refreshToken(refreshToken)
                            .build()
            );
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException(ex.getMessage());
        }
    }
}
