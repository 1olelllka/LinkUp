package com.olelllka.notification_service.repository;

import com.olelllka.notification_service.TestDataUtil;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
public class NotificationRepositoryTest {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));

    static {
        mongoDBContainer.start();
    }

    @AfterEach
    void refresh() {
        repository.deleteAll();
    }

    @AfterAll
    static void tearDown() {
        mongoDBContainer.stop();
        mongoDBContainer.close();
    }

    private final NotificationRepository repository;

    @Autowired
    public NotificationRepositoryTest(NotificationRepository repository) {
        this.repository = repository;
    }

    @Test
    public void testThatFindByUserIdWorksAsExpected() {
        NotificationEntity entity = repository.save(TestDataUtil.createNotificationEntity());
        Pageable pageable = PageRequest.of(0, 1);

        Page<NotificationEntity> result = repository.findByUserId(entity.getUserId(), pageable);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), 1)
        );
    }

}
