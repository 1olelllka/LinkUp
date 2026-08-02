package com.olelllka.profile_service.service;

import com.olelllka.profile_service.RabbitMQTestConfig;
import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.dto.UserMessageDto;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import com.olelllka.profile_service.repository.ProfileRepository;
import com.redis.testcontainers.RedisContainer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(RabbitMQTestConfig.class)
class MessageListenerIntegrationTest {

    @ServiceConnection
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));
    @ServiceConnection
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>(DockerImageName.parse("neo4j:latest"));
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:7.2.6"));
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));

    static {
        rabbitContainer.start();
        neo4jContainer.start();
        redisContainer.start();
        mongoDBContainer.start();
    }

    private final RabbitAdmin admin;
    private final ProfileRepository profileRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ProfileDocumentRepository documentRepository;

    @Autowired
    public MessageListenerIntegrationTest(RabbitAdmin admin,
                                          RabbitTemplate rabbitTemplate,
                                          ProfileRepository profileRepository,
                                          ProfileDocumentRepository documentRepository) {
        this.admin = admin;
        this.profileRepository = profileRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.documentRepository = documentRepository;
    }

    @AfterEach
    void refreshDBs() {
        profileRepository.deleteAll();
    }

    @AfterAll
    static void tearDown() {
        rabbitContainer.stop();
        rabbitContainer.close();
        neo4jContainer.stop();
        neo4jContainer.close();
        redisContainer.stop();
        redisContainer.close();
        mongoDBContainer.stop();
        mongoDBContainer.close();
    }

    @Test
    void testThatCreateProfileFromAuthServiceWorks() throws InterruptedException {
        // given
        UUID profileId = UUID.randomUUID();
        UserMessageDto userMessageDto = TestDataUtil.createUserMessageDto();
        userMessageDto.setProfileId(profileId);
        // when
        rabbitTemplate.convertAndSend(RabbitMQTestConfig.create_user_exchange, "create.user", userMessageDto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQTestConfig.create_user_queue).getMessageCount() == 0);
        Thread.sleep(Duration.of(2, ChronoUnit.SECONDS));
        // then
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> profileRepository.existsById(profileId));
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> documentRepository.existsById(profileId));
    }
}
