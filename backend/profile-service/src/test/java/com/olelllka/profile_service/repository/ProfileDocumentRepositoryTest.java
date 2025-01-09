package com.olelllka.profile_service.repository;

import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataElasticsearchTest
public class ProfileDocumentRepositoryTest {

    @ServiceConnection
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.23"));

    @Autowired
    private ProfileDocumentRepository repository;

    static {
        elasticsearchContainer.start();
    }

    @AfterAll
    static void tearDown() {
        elasticsearchContainer.stop();
        elasticsearchContainer.close();
    }

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void testThatFindByParamsFindsByTheWholeNameUsernameEmail() {
        ProfileDocument profileDocument = TestDataUtil.createNewProfileDocument();
        repository.save(profileDocument);
        Page<ProfileDocument> expected = new PageImpl<>(List.of(profileDocument));
        Pageable pageable = PageRequest.of(0, 1);

        Page<ProfileDocument> result1 = repository.findByParams(profileDocument.getName(), pageable);
        assertAll(
                () -> assertNotNull(result1),
                () -> assertEquals(result1.getContent().getFirst().getId(), expected.getContent().getFirst().getId())
        );

        Page<ProfileDocument> result2 = repository.findByParams(profileDocument.getUsername(), pageable);
        assertAll(
                () -> assertNotNull(result2),
                () -> assertEquals(result2.getContent().getFirst().getId(), expected.getContent().getFirst().getId())
        );

        Page<ProfileDocument> result3 = repository.findByParams(profileDocument.getEmail(), pageable);
        assertAll(
                () -> assertNotNull(result3),
                () -> assertEquals(result3.getContent().getFirst().getId(), expected.getContent().getFirst().getId())
        );
    }

    @Test
    public void testThatFindByParamsFindsByNotFullNameUsernameEmail() {
        ProfileDocument profileDocument = TestDataUtil.createNewProfileDocument();
        repository.save(profileDocument);
        Page<ProfileDocument> expected = new PageImpl<>(List.of(profileDocument));
        Pageable pageable = PageRequest.of(0, 1);

        Page<ProfileDocument> result1 = repository.findByParams("Full", pageable);
        assertAll(
                () -> assertNotNull(result1),
                () -> assertEquals(result1.getContent().getFirst().getId(), expected.getContent().getFirst().getId())
        );

        Page<ProfileDocument> result2 = repository.findByParams("use", pageable);
        assertAll(
                () -> assertNotNull(result2),
                () -> assertEquals(result2.getContent().getFirst().getId(), expected.getContent().getFirst().getId())
        );

        Page<ProfileDocument> result3 = repository.findByParams("email@", pageable);
        assertAll(
                () -> assertNotNull(result3),
                () -> assertEquals(result3.getContent().getFirst().getId(), expected.getContent().getFirst().getId())
        );
    }

    @Test
    public void testThatFindByParamsFindsBySynonymsNameUsernameEmail() {
        ProfileDocument profileDocument = TestDataUtil.createNewProfileDocument();
        repository.save(profileDocument);
        Page<ProfileDocument> expected = new PageImpl<>(List.of(profileDocument));
        Pageable pageable = PageRequest.of(0, 1);

        Page<ProfileDocument> result1 = repository.findByParams("Full Surname", pageable);
        assertAll(
                () -> assertNotNull(result1),
                () -> assertEquals(result1.getContent().getFirst().getId(), expected.getContent().getFirst().getId())
        );
    }

    @Test
    public void testThatFindByParamsDoesNotFindAnything() {
        Pageable pageable = PageRequest.of(0, 1);

        Page<ProfileDocument> result1 = repository.findByParams("Full Surname", pageable);
        assertAll(
                () -> assertNotNull(result1),
                () -> assertEquals(result1.getTotalElements(), 0)
        );
    }
}
