package com.olelllka.profile_service;

import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import com.olelllka.profile_service.domain.dto.ProfileDto;
import com.olelllka.profile_service.domain.dto.UserMessageDto;
import com.olelllka.profile_service.domain.entity.Gender;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.domain.entity.ProfileEntity;

import java.time.LocalDate;
import java.util.UUID;

public class TestDataUtil {

    public static ProfileEntity createNewProfileEntity() {
        return ProfileEntity.builder()
                .email("email@email.com")
                .name("Full Name")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .photo("Photo url")
                .username("username")
                .aboutMe("About me")
                .build();
    }

    public static UserMessageDto createUserMessageDto() {
        return UserMessageDto.builder()
                .email("email@email.com")
                .username("username")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .name("Full Name")
                .gender(Gender.MALE)
                .build();
    }

    public static ProfileDocument createNewProfileDocument() {
        return ProfileDocument.builder()
                .id(UUID.randomUUID())
                .email("email@email.com")
                .name("Full Name")
                .photo("Photo url")
                .username("username1234")
                .build();
    }

    public static ProfileDocumentDto createNewProfileDocumentDto() {
        return ProfileDocumentDto.builder()
                .id(UUID.randomUUID())
                .email("email@email.com")
                .name("Full Name")
                .photo("Photo url")
                .username("username1234")
                .build();
    }

    public static ProfileDto createNewProfileDto() {
        return ProfileDto.builder()
                .email("email@email.com")
                .name("Full Name")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .photo("Photo url")
                .username("username1234")
                .aboutMe("About me")
                .build();
    }

    public static PatchProfileDto createPatchProfileDto() {
        return PatchProfileDto.builder()
                .name("Full Name")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .photo("Photo url")
                .aboutMe("About me")
                .build();
    }

}
