package com.olelllka.auth_service.service;

import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.domain.dto.PatchUserDto;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.DuplicateException;
import com.olelllka.auth_service.service.impl.ProfileCacheHandlers;
import com.olelllka.auth_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Mock
    private ProfileCacheHandlers profileCacheHandlers;
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
        when(jwtUtil.extractId(jwt)).thenReturn("email@email.com");
        when(profileCacheHandlers.patchUserById("email@email.com", patchUserDto)).thenReturn(expected);
        UserEntity result = userService.patchUser(jwt, patchUserDto);
        // then
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
        when(jwtUtil.extractId(jwt)).thenReturn(expected.getEmail());
        when(profileCacheHandlers.getUserById(expected.getEmail())).thenReturn(expected);
        UserEntity result = userService.getUserByJwt(jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getUserId(), expected.getUserId())
        );
    }

}
