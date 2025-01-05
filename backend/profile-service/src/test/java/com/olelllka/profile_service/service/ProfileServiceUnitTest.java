package com.olelllka.profile_service.service;

import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.entity.Gender;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.olelllka.profile_service.rest.exception.NotFoundException;
import com.olelllka.profile_service.rest.exception.ValidationException;
import com.olelllka.profile_service.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
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
        when(repository.findByIdWithRelationships(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.getProfileById(id));
    }

    @Test
    public void testThatGetProfileByIdReturnsProfile() {
        // given
        UUID id = UUID.randomUUID();
        ProfileEntity profile = TestDataUtil.createNewProfileEntity();
        // when
        when(repository.findByIdWithRelationships(id)).thenReturn(Optional.of(profile));
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
        when(repository.existsById(id)).thenReturn(false);
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
        when(repository.existsById(id)).thenReturn(true);
        when(repository.updateProfile(id,
                expected.getUsername(),
                expected.getName(),
                expected.getEmail(),
                expected.getGender().toString(),
                expected.getPhoto(),
                expected.getAboutMe(),
                expected.getDateOfBirth())).thenReturn(expected);
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
        verify(repository, times(1)).updateProfile(id,
                expected.getUsername(),
                expected.getName(),
                expected.getEmail(),
                expected.getGender().toString(),
                expected.getPhoto(),
                expected.getAboutMe(),
                expected.getDateOfBirth());
    }

    @Test
    public void testThatServicePerformsDeleteCorrectly() {
        // given
        UUID uid = UUID.randomUUID();
        // when
        service.deleteById(uid);
        // then
        verify(repository, times(1)).deleteById(uid);
    }

    @Test
    public void testThatFollowNewProfileThrowsExceptions() {
        UUID id = UUID.randomUUID();
        assertThrows(ValidationException.class, () -> service.followNewProfile(id, id));
        UUID id2 = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> service.followNewProfile(id, id2));
        when(repository.existsById(id)).thenReturn(true);
        when(repository.existsById(id2)).thenReturn(true);
        when(repository.isFollowing(id, id2)).thenReturn(true);
        assertThrows(ValidationException.class, () -> service.followNewProfile(id, id2));
    }

    @Test
    public void testThatFollowNewProfileWorks() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(repository.existsById(id1)).thenReturn(true);
        when(repository.existsById(id2)).thenReturn(true);
        when(repository.isFollowing(id1, id2)).thenReturn(false);
        service.followNewProfile(id1, id2);
        verify(repository, times(1)).follow(id1, id2);
    }

    @Test
    public void testThatUnfollowProfileThrowsException() {
        UUID id = UUID.randomUUID();
        assertThrows(ValidationException.class, () -> service.unfollowProfile(id, id));
        UUID id2 = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> service.unfollowProfile(id, id2));
        when(repository.existsById(id)).thenReturn(true);
        when(repository.existsById(id2)).thenReturn(true);
        when(repository.isFollowing(id, id2)).thenReturn(false);
        assertThrows(ValidationException.class, () -> service.unfollowProfile(id, id2));
    }

    @Test
    public void testThatUnfollowProfileWorks() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(repository.existsById(id1)).thenReturn(true);
        when(repository.existsById(id2)).thenReturn(true);
        when(repository.isFollowing(id1, id2)).thenReturn(true);
        service.unfollowProfile(id1, id2);
        verify(repository, times(1)).unfollow(id1, id2);
    }

    @Test
    public void testThatGetFollowersByIdReturnsPageOfResults() {
        // given
        Pageable pageable = PageRequest.of(0, 1);
        Page<ProfileEntity> profiles = new PageImpl<>(List.of());
        UUID id = UUID.randomUUID();
        // when
        when(repository.findAllFollowersByProfileId(id, pageable)).thenReturn(profiles);
        Page<ProfileEntity> result = service.getFollowersById(id, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), 0)
        );
    }

    @Test
    public void testThatGetFolloweesByIdReturnsPageOfResults() {
        // given
        Pageable pageable = PageRequest.of(0, 1);
        Page<ProfileEntity> profiles = new PageImpl<>(List.of());
        UUID id = UUID.randomUUID();
        // when
        when(repository.findAllFolloweeByProfileId(id, pageable)).thenReturn(profiles);
        Page<ProfileEntity> result = service.getFolloweesById(id, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), 0)
        );
    }

    @Test
    public void testThatSearchUserByNeo4jWorks() {
        // given
        Pageable pageable = PageRequest.of(0, 1);
        Page<ProfileEntity> profiles = new PageImpl<>(List.of());
        String query = "search";
        // when
        when(repository.findProfileByParam(query, pageable)).thenReturn(profiles);
        Page<ProfileEntity> result = service.searchForProfile(query, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), profiles.getTotalElements())
        );
    }
}
