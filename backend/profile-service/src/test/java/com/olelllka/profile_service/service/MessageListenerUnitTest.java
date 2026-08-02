package com.olelllka.profile_service.service;

import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.dto.UserMessageDto;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import com.olelllka.profile_service.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageListenerUnitTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfileDocumentRepository documentRepository;
    @InjectMocks
    private MessageListener messageListener;

    @Test
    void testThatCreateProfileFromAuthServiceWorks() {
        // given
        UserMessageDto userMessageDto = TestDataUtil.createUserMessageDto();
        userMessageDto.setProfileId(UUID.randomUUID());
        ProfileEntity expectedEntity = ProfileEntity.builder().build();
        expectedEntity.setId(userMessageDto.getProfileId());
        expectedEntity.setName(userMessageDto.getName());
        expectedEntity.setUsername(userMessageDto.getUsername());
        expectedEntity.setGender(userMessageDto.getGender());
        expectedEntity.setCreatedAt(LocalDate.now());
        expectedEntity.setAboutMe(null);
        expectedEntity.setPhoto(null);
        expectedEntity.setDateOfBirth(userMessageDto.getDateOfBirth());
        when(profileRepository.save(any(ProfileEntity.class))).thenAnswer(a -> a.getArgument(0));
        // when
        messageListener.createProfileFromAuthService(userMessageDto);
        // then
        verify(profileRepository, times(1)).save(expectedEntity);
        verify(documentRepository, times(1)).save(any(ProfileDocument.class));
    }
}
