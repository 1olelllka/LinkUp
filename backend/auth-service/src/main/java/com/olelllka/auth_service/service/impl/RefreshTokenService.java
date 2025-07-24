package com.olelllka.auth_service.service.impl;

import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import com.olelllka.auth_service.service.AuthService;
import com.olelllka.auth_service.service.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class RefreshTokenService implements AuthService {

    private final JWTUtil jwtUtil;

    @Override
    public Optional<String> refreshToken(String token) {
        try {
            Claims claims = jwtUtil.getClaims(token);
            String sub = claims.getSubject();
            if (claims.getExpiration().after(Date.from(Instant.now()))) {
                return Optional.of(jwtUtil.generateRefreshJWT(UUID.fromString(sub)));
            }
        } catch (JwtException | IllegalArgumentException ex) {
            throw new UnauthorizedException(ex.getMessage());
        }
        return Optional.empty();
    }
}
