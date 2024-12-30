package com.olelllka.profile_service.service;

import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.entity.Gender;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.olelllka.profile_service.rest.exception.NotFoundException;
import com.olelllka.profile_service.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceUnitTest {

    @Mock
    private ProfileRepository repository;
    @InjectMocks
    private ProfileServiceImpl service;

    @Test
    public void testThatProfileServiceCreatesNewProfile() {
        // given
        ProfileEntity profile = TestDataUtil.createNewProfileEntity();
        ProfileEntity expected = TestDataUtil.createNewProfileEntity();
        expected.setCreatedAt(LocalDate.now());
        // when
        when(repository.save(expected)).thenReturn(expected);
        ProfileEntity result = service.createProfile(profile);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getCreatedAt(), LocalDate.now())
        );
    }

    @Test
    public void testThatGetProfileByIdThrowsException() {
        // given
        UUID id = UUID.randomUUID();
        // when
        when(repository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.getProfileById(id));
    }

    @Test
    public void testThatGetProfileByIdReturnsProfile() {
        // given
        UUID id = UUID.randomUUID();
        ProfileEntity profile = TestDataUtil.createNewProfileEntity();
        // when
        when(repository.findById(id)).thenReturn(Optional.of(profile));
        ProfileEntity result = service.getProfileById(id);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getUsername(), profile.getUsername())
        );
    }

    @Test
    public void testThatUpdateProfileThrowsException() {
        // given
        UUID id = UUID.randomUUID();
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        // when
        when(repository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.updateProfile(id, patchProfileDto));
        verify(repository, never()).save(any(ProfileEntity.class));
    }

    @Test
    public void testThatUpdateProfileUpdatesTheProfile() {
        // given
        UUID id = UUID.randomUUID();
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        patchProfileDto.setName("UPDATED NAME");
        patchProfileDto.setUsername("UPDATED USERNAME");
        patchProfileDto.setEmail("UPDATED EMAIL");
        patchProfileDto.setDateOfBirth(LocalDate.of(2021, 1, 1));
        patchProfileDto.setGender(Gender.FEMALE);
        patchProfileDto.setAboutMe("UPDATED");
        patchProfileDto.setPhoto("UPDATED PHOTO");
        ProfileEntity entity = TestDataUtil.createNewProfileEntity();
        ProfileEntity expected = TestDataUtil.createNewProfileEntity();
        expected.setName("UPDATED NAME");
        expected.setUsername("UPDATED USERNAME");
        expected.setEmail("UPDATED EMAIL");
        expected.setDateOfBirth(LocalDate.of(2021, 1, 1));
        expected.setGender(Gender.FEMALE);
        expected.setPhoto("UPDATED PHOTO");
        expected.setAboutMe("UPDATED");
        // when
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(expected)).thenReturn(expected);
        ProfileEntity result = service.updateProfile(id, patchProfileDto);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getName(), patchProfileDto.getName()),
                () -> assertEquals(result.getDateOfBirth(), patchProfileDto.getDateOfBirth()),
                () -> assertEquals(result.getEmail(), patchProfileDto.getEmail()),
                () -> assertEquals(result.getAboutMe(), patchProfileDto.getAboutMe()),
                () -> assertEquals(result.getGender(), patchProfileDto.getGender()),
                () -> assertEquals(result.getPhoto(), patchProfileDto.getPhoto()),
                () -> assertEquals(result.getPhoto(), patchProfileDto.getPhoto())
                );
    }

}
