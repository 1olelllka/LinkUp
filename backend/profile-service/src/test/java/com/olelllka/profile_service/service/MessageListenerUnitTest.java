package com.olelllka.profile_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.dto.UserMessageDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageListenerUnitTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private RedisTemplate<String, ProfileEntity> redisTemplate;
    @InjectMocks
    private MessageListener messageListener;

    @Test
    public void testThatCreateProfileFromAuthServiceWorks() {
        // given
        UserMessageDto userMessageDto = TestDataUtil.createUserMessageDto();
        userMessageDto.setProfileId(UUID.randomUUID());
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
    }

    @Test
    public void testThatUpdateProfileFromAuthServiceWorks() throws JsonProcessingException {
        UUID profileId = UUID.randomUUID();
        UserMessageDto messageDto = TestDataUtil.createUserMessageDto();
        messageDto.setProfileId(profileId);
        ProfileEntity expectedEntity = ProfileEntity.builder()
                .id(messageDto.getProfileId())
                .username(messageDto.getUsername())
                .email(messageDto.getEmail()).build();
        expectedEntity.setEmail(messageDto.getEmail());
        // when
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(profileRepository.updateProfile(profileId, messageDto.getUsername(), null,
                messageDto.getEmail(), null, null, null, null)).thenReturn(expectedEntity);
        messageListener.updateProfileFromAuthService(messageDto);
        verify(profileRepository, times(1)).updateProfile(profileId, messageDto.getUsername(), null, messageDto.getEmail(), null, null, null, null);
        verify(redisTemplate.opsForValue(), times(1))
                .set("profile::" + SHA256.hash(messageDto.getProfileId().toString()), expectedEntity, 60, TimeUnit.MINUTES);
    }
}
