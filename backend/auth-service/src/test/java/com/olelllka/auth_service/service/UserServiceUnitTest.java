package com.olelllka.auth_service.service;

import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.domain.dto.JWTToken;
import com.olelllka.auth_service.domain.dto.PatchUserDto;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.DuplicateException;
import com.olelllka.auth_service.service.impl.UserServiceImpl;
import org.apache.catalina.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MessagePublisher messagePublisher;
    @Mock
    private JWTUtil jwtUtil;
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

    @Test
    public void testThatPatchUserWorksFine() {
        // given
        String jwt = "jwt";
        PatchUserDto patchUserDto = PatchUserDto.builder()
                .email("newemail@email.com")
                .build();
        UserEntity expected = TestDataUtil.createUserEntity();
        expected.setEmail(patchUserDto.getEmail());
        // when
        when(jwtUtil.extractUsername(jwt)).thenReturn("email@email.com");
        when(userRepository.findByEmail("email@email.com")).thenReturn(Optional.of(TestDataUtil.createUserEntity()));
        when(userRepository.save(expected)).thenReturn(expected);
        UserEntity result = userService.patchUser(jwt, patchUserDto);
        // then
        UserMessageDto userMessageDto = UserMessageDto.builder().email(patchUserDto.getEmail()).username(expected.getUsername()).build();
        verify(messagePublisher, times(1)).sendUpdateUserMessage(userMessageDto);
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getEmail(), expected.getEmail())
        );
    }

    @Test
    public void testThatGetUserByJWTWorksFind() {
        // given
        String jwt = "jwt";
        UserEntity expected = TestDataUtil.createUserEntity();
        // when
        when(jwtUtil.extractUsername(jwt)).thenReturn(expected.getEmail());
        when(userRepository.findByEmail(expected.getEmail())).thenReturn(Optional.of(expected));
        UserEntity result = userService.getUserByJwt(jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getUserId(), expected.getUserId())
        );
    }

}
