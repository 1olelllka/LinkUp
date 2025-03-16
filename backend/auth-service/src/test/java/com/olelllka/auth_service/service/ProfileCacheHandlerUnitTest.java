package com.olelllka.auth_service.service;

import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.domain.dto.PatchUserDto;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileCacheHandlerUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MessagePublisher messagePublisher;
    @InjectMocks
    private ProfileCacheHandlers profileCacheHandlers;

    @Test
    public void testThatGetUserByEmailThrowsException() {
        // given
        UUID id = UUID.randomUUID();
        // when
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> profileCacheHandlers.getUserById(id.toString()));
    }

    @Test
    public void testThatGetUserByEmailReturnsUser() {
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

    @Test
    public void testThatPatchUserByEmailThrowsException() {
        // given
        PatchUserDto patchUserDto = PatchUserDto.builder().alias("alias").email("newemail@email.com").build();
        UUID id = UUID.randomUUID();
        // when
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> profileCacheHandlers.patchUserById(id.toString(), patchUserDto));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    public void testThatPatchUserByEmailUpdatesTheUser() {
        // given
        PatchUserDto patchUserDto = PatchUserDto.builder().alias("alias").email("newemail@email.com").build();
        UUID id = UUID.randomUUID();
        UserEntity expected = TestDataUtil.createUserEntity();
        expected.setAlias(patchUserDto.getAlias());
        expected.setEmail(patchUserDto.getEmail());
        // when
        when(userRepository.findById(id)).thenReturn(Optional.of(TestDataUtil.createUserEntity()));
        when(userRepository.save(expected)).thenReturn(expected);
        UserEntity result = profileCacheHandlers.patchUserById(id.toString(), patchUserDto);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getAlias(), expected.getAlias()),
                () -> assertEquals(result.getEmail(), expected.getEmail())
        );
        UserMessageDto messageDto = TestDataUtil.createUserMessageDto();
        messageDto.setEmail(patchUserDto.getEmail());
        messageDto.setUsername(patchUserDto.getAlias());
        messageDto.setDateOfBirth(null);
        messageDto.setGender(null);
        messageDto.setName(null);
        verify(messagePublisher, times(1)).sendUpdateUserMessage(messageDto);
    }


}
