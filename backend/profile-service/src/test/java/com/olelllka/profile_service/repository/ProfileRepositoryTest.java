package com.olelllka.profile_service.repository;

import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataNeo4jTest
public class ProfileRepositoryTest {

    @ServiceConnection
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>(DockerImageName.parse("neo4j:latest"));

    @Autowired
    private ProfileRepository repository;

    static {
        neo4j.start();
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @AfterAll
    static void stopContainer() {
       neo4j.stop();
       neo4j.close();
    }

    @Test
    public void testThatCustomUpdateProfileWorks() {
        ProfileEntity profile = repository.save(TestDataUtil.createNewProfileEntity());
        ProfileEntity updatedProfile = TestDataUtil.createNewProfileEntity();
        updatedProfile.setName("UPDATED");

        ProfileEntity result = repository.updateProfile(profile.getId(),
                updatedProfile.getUsername(),
                updatedProfile.getName(),
                updatedProfile.getEmail(),
                updatedProfile.getGender().toString(),
                updatedProfile.getPhoto(),
                updatedProfile.getAboutMe(),
                updatedProfile.getDateOfBirth());

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getName(), updatedProfile.getName())
        );
    }

    @Test
    public void testThatFollowWorks() {
        ProfileEntity profile1 = repository.save(TestDataUtil.createNewProfileEntity());
        ProfileEntity profile2 = repository.save(TestDataUtil.createNewProfileEntity());
        repository.follow(profile1.getId(), profile2.getId());
        boolean check = repository.isFollowing(profile1.getId(), profile2.getId());
        assertTrue(check);
    }

    @Test
    public void testThatUnfollowWorks() {
        ProfileEntity profile1 = repository.save(TestDataUtil.createNewProfileEntity());
        ProfileEntity profile2 = repository.save(TestDataUtil.createNewProfileEntity());
        repository.follow(profile1.getId(), profile2.getId());
        boolean check = repository.isFollowing(profile1.getId(), profile2.getId());
        assertTrue(check);
        repository.unfollow(profile1.getId(), profile2.getId());
        boolean check2 = repository.isFollowing(profile1.getId(), profile2.getId());
        assertFalse(check2);
    }

    @Test
    public void testThatFindAllFolloweeByProfileIdWorks() {
        ProfileEntity profile1 = repository.save(TestDataUtil.createNewProfileEntity());
        ProfileEntity profile2 = repository.save(TestDataUtil.createNewProfileEntity());
        repository.follow(profile1.getId(), profile2.getId());
        Pageable pageable = PageRequest.of(0, 1);
        Page<ProfileEntity> expected = new PageImpl<>(List.of(profile2));

        Page<ProfileEntity> result = repository.findAllFolloweeByProfileId(profile1.getId(), pageable);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), expected.getTotalElements()),
                () -> assertEquals(result.getContent().getFirst().getId(), profile2.getId())
        );
    }

    @Test
    public void testThatFindAllFollowersByProfileIdWorks() {
        ProfileEntity profile1 = repository.save(TestDataUtil.createNewProfileEntity());
        ProfileEntity profile2 = repository.save(TestDataUtil.createNewProfileEntity());
        repository.follow(profile1.getId(), profile2.getId());
        Pageable pageable = PageRequest.of(0, 1);
        Page<ProfileEntity> expected = new PageImpl<>(List.of(profile1));

        Page<ProfileEntity> result = repository.findAllFollowersByProfileId(profile2.getId(), pageable);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), expected.getTotalElements()),
                () -> assertEquals(result.getContent().getFirst().getId(), profile1.getId())
        );
    }

    @Test
    public void testThatFindProfilesByParamWorks() {
        ProfileEntity profile1 = repository.save(TestDataUtil.createNewProfileEntity());
        ProfileEntity profile2 = repository.save(TestDataUtil.createNewProfileEntity());
        profile2.setUsername("u2");
        repository.save(profile1);
        repository.save(profile2);
        Pageable pageable = PageRequest.of(0, 2);

        Page<ProfileEntity> result = repository.findProfileByParam("u", pageable);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), 2)
        );
    }
}
