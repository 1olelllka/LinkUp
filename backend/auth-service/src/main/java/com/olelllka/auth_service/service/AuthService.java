package com.olelllka.auth_service.service;

import java.util.Optional;

public interface AuthService {
    Optional<String> refreshToken(String token);
}
