package com.olelllka.auth_service.service;

import com.olelllka.auth_service.domain.dto.JWTTokenResponse;

import java.util.Optional;

public interface AuthService {
    Optional<JWTTokenResponse> refreshToken(String token);
}
