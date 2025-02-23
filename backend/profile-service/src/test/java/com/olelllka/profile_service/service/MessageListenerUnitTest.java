package com.olelllka.profile_service.service;

import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageListenerUnitTest {

    @Mock
    private ProfileDocumentRepository documentRepository;
    @Mock
    private ProfileRepository profileRepository;
    @InjectMocks
    private MessageListener messageListener;

    @Test
    public void testThatCreateProfileFromAuthServiceWorks() {
        // given
        UserMessageDto userMessageDto = TestDataUtil.createUserMessageDto();
        userMessageDto.setProfileId(UUID.randomUUID());
        ProfileDocument expectedDocument = TestDataUtil.createNewProfileDocument();
        expectedDocument.setPhoto("");
        expectedDocument.setUsername(userMessageDto.getUsername());
        expectedDocument.setId(userMessageDto.getProfileId());
        ProfileEntity expectedEntity = ProfileEntity.builder().build();
        expectedEntity.setId(userMessageDto.getProfileId());
        expectedEntity.setName(userMessageDto.getName());
        expectedEntity.setEmail(userMessageDto.getEmail());
        expectedEntity.setUsername(userMessageDto.getUsername());
        expectedEntity.setGender(userMessageDto.getGender());
        expectedEntity.setCreatedAt(LocalDate.now());
        expectedEntity.setAboutMe(null);
        expectedEntity.setPhoto(null);
        expectedEntity.setDateOfBirth(userMessageDto.getDateOfBirth());
        // when
        messageListener.createProfileFromAuthService(userMessageDto);
        // then
        verify(profileRepository, times(1)).save(expectedEntity);
        verify(documentRepository, times(1)).save(expectedDocument);
    }

    @Test
    public void testThatUpdateProfileFromAuthServiceWorks() {
        UUID profileId = UUID.randomUUID();
        UserMessageDto messageDto = TestDataUtil.createUserMessageDto();
        messageDto.setProfileId(profileId);
        ProfileEntity expectedEntity = ProfileEntity.builder()
                .id(messageDto.getProfileId())
                .username(messageDto.getUsername())
                .email(messageDto.getEmail()).build();
        ProfileDocument profileDocument = TestDataUtil.createNewProfileDocument();
        profileDocument.setId(messageDto.getProfileId());
        ProfileDocument expectedDocument = TestDataUtil.createNewProfileDocument();
        expectedDocument.setId(messageDto.getProfileId());
        expectedDocument.setUsername(messageDto.getUsername());
        expectedEntity.setEmail(messageDto.getEmail());
        // when
        when(documentRepository.findById(profileId)).thenReturn(Optional.of(profileDocument));
        messageListener.updateProfileFromAuthService(messageDto);
        verify(documentRepository, times(1)).save(expectedDocument);
        verify(profileRepository, times(1)).updateProfile(profileId, messageDto.getUsername(), null, messageDto.getEmail(), null, null, null, null);
    }

    @Test
    public void testThatUpdateProfileOnElasticsearchSavesRightValues() {
        // given
        ProfileDocumentDto dto = TestDataUtil.createNewProfileDocumentDto();
        ProfileDocument expected = TestDataUtil.createNewProfileDocument();
        expected.setId(dto.getId());
        expected.setUsername(null);
        expected.setEmail(null);
        // when
        messageListener.updateProfileOnElasticsearch(dto);
        // then
        verify(documentRepository, times(1)).save(expected);
    }

    @Test
    public void testThatDeleteProfileOnElasticSearch() {
        // given
        UUID id = UUID.randomUUID();
        // when
        messageListener.deleteProfileOnElasticSearch(id);
        // then
        verify(documentRepository, times(1)).deleteById(id);
    }
}
