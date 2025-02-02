package com.olelllka.stories_service.service;

import com.olelllka.stories_service.RabbitMQConfig;
import com.olelllka.stories_service.TestDataUtil;
import com.olelllka.stories_service.domain.entity.StoryEntity;
import com.olelllka.stories_service.repository.StoryRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(RabbitMQConfig.class)
public class RabbitMQListenerIntegrationTest {

    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.0");
    @ServiceConnection
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

    static {
        mongoDBContainer.start();
        rabbitContainer.start();
    }

    @AfterAll
    static void tearDown() {
        mongoDBContainer.stop();
        mongoDBContainer.close();
        rabbitContainer.stop();
        rabbitContainer.close();
    }

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin admin;
    private final StoryRepository repository;

    @Autowired
    public RabbitMQListenerIntegrationTest(RabbitTemplate rabbitTemplate,
                                           RabbitAdmin admin,
                                           StoryRepository repository) {
        this.rabbitTemplate = rabbitTemplate;
        this.repository = repository;
        this.admin = admin;
    }

    @Test
    public void testThatRabbitListenerWorksAsExpected() {
        UUID profileId = UUID.randomUUID();
        StoryEntity story = TestDataUtil.createStoryEntity();
        story.setUserId(profileId);
        repository.save(story);
        Pageable pageable = PageRequest.of(0, 1);
        Page<StoryEntity> check1 = repository.findStoryByUserId(profileId, pageable);
        assertEquals(check1.getTotalElements(), 1);
        rabbitTemplate.convertAndSend("profile_exchange", "delete_profile", profileId);
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> admin.getQueueInfo("delete_profile_queue").getMessageCount() == 0);
        Page<StoryEntity> check2 = repository.findStoryByUserId(profileId, pageable);
        assertEquals(check2.getTotalElements(), 0);
    }

}
