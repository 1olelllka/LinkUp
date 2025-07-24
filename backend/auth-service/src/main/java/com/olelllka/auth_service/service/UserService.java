package com.olelllka.auth_service.service;

import com.olelllka.auth_service.domain.dto.JWTTokenResponse;
import com.olelllka.auth_service.domain.dto.PatchUserDto;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.entity.UserEntity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public interface UserService {
    UserEntity registerUser(RegisterUserDto userDto);

    UserEntity patchUser(String jwt, PatchUserDto patchUserDto);

    UserEntity getUserByJwt(String jwt);

    JWTTokenResponse generateJWTViaEmail(@Email(message = "Invalid Email.") @NotEmpty(message = "Email must not be empty.") String email);
}
