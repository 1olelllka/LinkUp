package com.olelllka.auth_service.service;

import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.DuplicateException;
import com.olelllka.auth_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MessagePublisher messagePublisher;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;


    @Test
    public void testThatRegisterUserThrowsExceptionIfDuplicate() {
        // given
        RegisterUserDto registerUserDto = TestDataUtil.createRegisterUserDto();
        // when
        when(userRepository.existsByEmail(registerUserDto.getEmail())).thenReturn(true);
        // then
        assertThrows(DuplicateException.class, () -> userService.registerUser(registerUserDto));
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(messagePublisher, never()).sendCreateUserMessage(any(UserMessageDto.class));
    }

}
