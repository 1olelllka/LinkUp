package com.olelllka.auth_service.service;

import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.entity.UserEntity;

public interface UserService {
    UserEntity registerUser(RegisterUserDto userDto);
}
