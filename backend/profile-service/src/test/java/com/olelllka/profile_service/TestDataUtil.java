package com.olelllka.profile_service;

import com.olelllka.profile_service.domain.dto.CreateProfileDto;
import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import com.olelllka.profile_service.domain.dto.ProfileDto;
import com.olelllka.profile_service.domain.entity.Gender;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.domain.entity.ProfileEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TestDataUtil {

    public static ProfileEntity createNewProfileEntity() {
        return ProfileEntity.builder()
                .email("email@email.com")
                .name("Full Name")
                .gender(Gender.MALE)
                .password("Password")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .photo("Photo url")
                .username("username")
                .aboutMe("About me")
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
                .password("Password")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .photo("Photo url")
                .username("username1234")
                .aboutMe("About me")
                .build();
    }

    public static CreateProfileDto createNewCreateProfileDto() {
        return CreateProfileDto.builder()
                .email("email@email.com")
                .name("Name")
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .gender(Gender.MALE)
                .password("Password")
                .username("username1234")
                .build();
    }

    public static PatchProfileDto createPatchProfileDto() {
        return PatchProfileDto.builder()
                .email("email@email.com")
                .name("Full Name")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(2020, 1, 1))
                .photo("Photo url")
                .username("username1234")
                .aboutMe("About me")
                .build();
    }

}
