package com.olelllka.auth_service.service;

import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.NotFoundException;
import com.olelllka.auth_service.service.impl.ProfileCacheHandlers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileCacheHandlerUnitTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ProfileCacheHandlers profileCacheHandlers;

    @Test
    void testThatGetUserByEmailThrowsException() {
        // given
        UUID id = UUID.randomUUID();
        // when
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> profileCacheHandlers.getUserById(id.toString()));
    }

    @Test
    void testThatGetUserByEmailReturnsUser() {
        // given
        UserEntity user = TestDataUtil.createUserEntity();
        UUID id = UUID.randomUUID();
        // when
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        UserEntity result = profileCacheHandlers.getUserById(id.toString());
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getEmail(), user.getEmail())
        );
    }
}
