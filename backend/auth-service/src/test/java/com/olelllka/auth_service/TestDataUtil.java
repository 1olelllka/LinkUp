package com.olelllka.auth_service;

import com.olelllka.auth_service.domain.dto.Gender;
import com.olelllka.auth_service.domain.dto.LoginUser;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.AuthProvider;
import com.olelllka.auth_service.domain.entity.Role;
import com.olelllka.auth_service.domain.entity.UserEntity;

import java.time.LocalDate;

public class TestDataUtil {

    public static RegisterUserDto createRegisterUserDto() {
        return RegisterUserDto.builder()
                .email("email@email.com")
                .name("name")
                .username("username")
                .gender(Gender.UNDEFINED)
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .password("password")
                .build();
    }

    public static LoginUser createLoginUser() {
        return LoginUser.builder()
                .email("email@email.com")
                .password("password")
                .build();
    }

    public static UserEntity createUserEntity() {
        return UserEntity.builder()
                .email("email@email.com")
                .username("username")
                .authProvider(AuthProvider.LOCAL)
                .providerId("")
                .role(Role.USER)
                .build();
    }

    public static UserMessageDto createUserMessageDto() {
        return UserMessageDto.builder()
                .email("email@email.com")
                .name("name")
                .username("username")
                .gender(Gender.UNDEFINED)
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .build();
    }
}
