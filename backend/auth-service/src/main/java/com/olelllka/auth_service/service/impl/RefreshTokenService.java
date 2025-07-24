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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements AuthService {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Optional<JWTTokenResponse> refreshToken(String token) {
        if (redisTemplate.hasKey("refresh_token:" + token)) {
                try {
                    Claims claims = jwtUtil.getClaims(token);
                    String sub = claims.getSubject();
                    if (claims.getExpiration().after(Date.from(Instant.now()))) {
                        String refreshToken = jwtUtil.generateRefreshJWT(UUID.fromString(sub));
                        redisTemplate.delete("refresh_token:" + token);
                        redisTemplate.opsForValue().set("refresh_token:" + refreshToken, "", Duration.of(1, ChronoUnit.DAYS));
                        return Optional.of(
                                JWTTokenResponse.builder()
                                        .accessToken(jwtUtil.generateAccessJWT(UUID.fromString(sub)))
                                        .refreshToken(refreshToken)
                                        .build()
                        );
                    }
            } catch(JwtException | IllegalArgumentException ex){
                throw new UnauthorizedException(ex.getMessage());
            }
        }
        throw new UnauthorizedException("You are unauthorized to perform such action.");
    }
}
