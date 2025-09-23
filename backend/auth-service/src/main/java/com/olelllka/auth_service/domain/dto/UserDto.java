package com.olelllka.auth_service.domain.dto;

import com.olelllka.auth_service.domain.entity.AuthProvider;
import com.olelllka.auth_service.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {
    private UUID userId;
    private String alias;
    private String email;
    private String password;
    private AuthProvider authProvider;
    private String providerId;
    private Role role;
}
