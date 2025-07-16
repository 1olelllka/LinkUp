package com.olelllka.profile_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.dto.NotificationDto;
import com.olelllka.profile_service.domain.dto.PatchProfileDto;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import com.olelllka.profile_service.domain.entity.Gender;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.olelllka.profile_service.rest.exception.AuthException;
import com.olelllka.profile_service.rest.exception.NotFoundException;
import com.olelllka.profile_service.rest.exception.ValidationException;
import com.olelllka.profile_service.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.elasticsearch.ElasticsearchRestClientHealthIndicator;
import org.springframework.boot.actuate.health.Health;
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
    @Mock
    private ProfileDocumentRepository elasticRepository;
    @Mock
    private ElasticsearchRestClientHealthIndicator elasticHealth;
    @Mock
    private MessagePublisher messagePublisher;
    @Mock
    private JWTUtil jwtUtil;
    @InjectMocks
    private ProfileServiceImpl service;

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
        UUID id = UUID.randomUUID();
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        String jwt = "jwt";

        when(repository.existsById(id)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.updateProfile(id, patchProfileDto, jwt));
        when(repository.existsById(id)).thenReturn(true);
        when(jwtUtil.extractId(jwt)).thenReturn("incorrect");
        assertThrows(AuthException.class, () -> service.updateProfile(id, patchProfileDto, jwt));

        verify(repository, never()).save(any(ProfileEntity.class));
    }

    @Test
    public void testThatUpdateProfileUpdatesTheProfile() {
        // given
        UUID id = UUID.randomUUID();
        PatchProfileDto patchProfileDto = TestDataUtil.createPatchProfileDto();
        patchProfileDto.setName("UPDATED NAME");
        patchProfileDto.setDateOfBirth(LocalDate.of(2021, 1, 1));
        patchProfileDto.setGender(Gender.FEMALE);
        patchProfileDto.setAboutMe("UPDATED");
        patchProfileDto.setPhoto("UPDATED PHOTO");
        ProfileEntity expected = TestDataUtil.createNewProfileEntity();
        expected.setName("UPDATED NAME");
        expected.setDateOfBirth(LocalDate.of(2021, 1, 1));
        expected.setGender(Gender.FEMALE);
        expected.setPhoto("UPDATED PHOTO");
        expected.setAboutMe("UPDATED");
        String jwt = "jwt";
        // when
        when(repository.existsById(id)).thenReturn(true);
        when(jwtUtil.extractId(jwt)).thenReturn(id.toString());
        when(repository.updateProfile(id,
                null,
                expected.getName(),
                null,
                expected.getGender().toString(),
                expected.getPhoto(),
                expected.getAboutMe(),
                expected.getDateOfBirth())).thenReturn(expected);
        ProfileEntity result = service.updateProfile(id, patchProfileDto, jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getName(), patchProfileDto.getName()),
                () -> assertEquals(result.getDateOfBirth(), patchProfileDto.getDateOfBirth()),
                () -> assertEquals(result.getAboutMe(), patchProfileDto.getAboutMe()),
                () -> assertEquals(result.getGender(), patchProfileDto.getGender()),
                () -> assertEquals(result.getPhoto(), patchProfileDto.getPhoto()),
                () -> assertEquals(result.getPhoto(), patchProfileDto.getPhoto()));
        verify(repository, times(1)).updateProfile(id,
                null,
                expected.getName(),
                null,
                expected.getGender().toString(),
                expected.getPhoto(),
                expected.getAboutMe(),
                expected.getDateOfBirth());
        ProfileDocumentDto documentDto = ProfileDocumentDto.builder()
                .id(result.getId())
                .name(result.getName())
                .photo(result.getPhoto())
                .username(result.getUsername())
                .email(result.getEmail())
                .build();
        verify(messagePublisher, times(1)).updateProfile(documentDto);
    }

    @Test
    public void testThatServicePerformsDeleteCorrectly() {
        // given
        UUID uid = UUID.randomUUID();
        String jwt = "jwt";
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(uid.toString());
        service.deleteById(uid, jwt);
        // then
        verify(repository, times(1)).deleteById(uid);
        verify(messagePublisher, times(1)).deleteProfile(uid);
    }

    @Test
    public void testThatFollowNewProfileThrowsExceptions() {
        UUID id = UUID.randomUUID();
        String jwt = "jwt";
        assertThrows(ValidationException.class, () -> service.followNewProfile(id, id, jwt));
        UUID id2 = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> service.followNewProfile(id, id2, jwt));
        when(repository.existsById(id)).thenReturn(true);
        when(repository.existsById(id2)).thenReturn(true);
        when(repository.isFollowing(id, id2)).thenReturn(true);
        when(jwtUtil.extractId(jwt)).thenReturn(id.toString());
        assertThrows(ValidationException.class, () -> service.followNewProfile(id, id2, jwt));
        when(jwtUtil.extractId(jwt)).thenReturn("incorrect");
        assertThrows(AuthException.class, () -> service.followNewProfile(id, id2, jwt));
    }

    @Test
    public void testThatFollowNewProfileWorks() throws JsonProcessingException {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        String jwt = "jwt";
        ProfileEntity entity = TestDataUtil.createNewProfileEntity();
        when(repository.existsById(id1)).thenReturn(true);
        when(repository.existsById(id2)).thenReturn(true);
        when(repository.findByIdWithRelationships(id1)).thenReturn(Optional.of(entity));
        when(repository.isFollowing(id1, id2)).thenReturn(false);
        when(jwtUtil.extractId(jwt)).thenReturn(id1.toString());
        service.followNewProfile(id1, id2, jwt);
        verify(repository, times(1)).follow(id1, id2);
        verify(messagePublisher, times(1)).createFollowNotification(any(NotificationDto.class));
    }

    @Test
    public void testThatUnfollowProfileThrowsException() {
        UUID id = UUID.randomUUID();
        String jwt = "jwt";
        assertThrows(ValidationException.class, () -> service.unfollowProfile(id, id, jwt));
        UUID id2 = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> service.unfollowProfile(id, id2, jwt));
        when(repository.existsById(id)).thenReturn(true);
        when(repository.existsById(id2)).thenReturn(true);
        when(repository.isFollowing(id, id2)).thenReturn(false);
        when(jwtUtil.extractId(jwt)).thenReturn(id.toString());
        assertThrows(ValidationException.class, () -> service.unfollowProfile(id, id2, jwt));
        when(jwtUtil.extractId(jwt)).thenReturn("incorrect");
        assertThrows(AuthException.class, () -> service.unfollowProfile(id, id2, jwt));
    }

    @Test
    public void testThatUnfollowProfileWorks() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        String jwt = "jwt";
        when(repository.existsById(id1)).thenReturn(true);
        when(repository.existsById(id2)).thenReturn(true);
        when(repository.isFollowing(id1, id2)).thenReturn(true);
        when(jwtUtil.extractId(jwt)).thenReturn(id1.toString());
        service.unfollowProfile(id1, id2, jwt);
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
        when(elasticHealth.getHealth(false)).thenReturn(Health.down().build());
        when(repository.findProfileByParam(query, pageable)).thenReturn(profiles);
        Page<ProfileEntity> result = service.searchForProfile(query, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), profiles.getTotalElements())
        );
        verify(elasticRepository, never()).findByParams(query, pageable);
    }

    @Test
    public void testThatSearchUserByElasticsearchWorks() {
        // given
        Pageable pageable = PageRequest.of(0, 1);
        Page<ProfileEntity> profiles = new PageImpl<>(List.of());
        Page<ProfileDocument> elasticProfiles = new PageImpl<>(List.of());
        String query = "search";
        // when
        when(elasticHealth.getHealth(false)).thenReturn(Health.up().build());
        when(elasticRepository.findByParams(query, pageable)).thenReturn(elasticProfiles);
        Page<ProfileEntity> result = service.searchForProfile(query, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), profiles.getTotalElements())
        );
        verify(repository, never()).findProfileByParam(query, pageable);
    }
}
