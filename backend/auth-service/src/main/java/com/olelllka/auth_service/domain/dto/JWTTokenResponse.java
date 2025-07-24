package com.olelllka.auth_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JWTTokenResponse {
    private String accessToken;
    private String refreshToken;
}
