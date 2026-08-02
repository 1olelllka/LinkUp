package com.olelllka.profile_service.service;

import com.olelllka.profile_service.RabbitMQTestConfig;
import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.dto.UserMessageDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(RabbitMQTestConfig.class)
public class MessageListenerIntegrationTest {

    @ServiceConnection
    static RabbitMQContainer rabbitContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));
    @ServiceConnection
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>(DockerImageName.parse("neo4j:latest"));
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:7.2.6"));

    static {
        rabbitContainer.start();
        neo4jContainer.start();
        redisContainer.start();
    }

    private final RabbitAdmin admin;
    private final MessagePublisher messagePublisher;
    private final ProfileRepository profileRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, ProfileEntity> redisTemplate;

    @Autowired
    public MessageListenerIntegrationTest(RabbitAdmin admin,
                                          MessagePublisher messagePublisher,
                                          RabbitTemplate rabbitTemplate,
                                          ProfileRepository profileRepository,
                                          RedisTemplate<String, ProfileEntity> redisTemplate) {
        this.messagePublisher = messagePublisher;
        this.admin = admin;
        this.profileRepository = profileRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
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
    }

    @Test
    public void testThatCreateProfileFromAuthServiceWorks() throws InterruptedException {
        // given
        UUID profileId = UUID.randomUUID();
        UserMessageDto userMessageDto = TestDataUtil.createUserMessageDto();
        userMessageDto.setProfileId(profileId);
        // when
        rabbitTemplate.convertAndSend(RabbitMQTestConfig.create_user_exchange, "create.user", userMessageDto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQTestConfig.create_user_queue).getMessageCount() == 0);
        Thread.sleep(Duration.of(2, ChronoUnit.SECONDS));
        // then
        assertTrue(profileRepository.existsById(profileId));
    }

    @Test
    public void testThatUpdateProfileFromAuthServiceWorks() throws InterruptedException {
        UUID profileId = UUID.randomUUID();
        UserMessageDto userMessageDto = TestDataUtil.createUserMessageDto();
        userMessageDto.setProfileId(profileId);
        // create profile
        rabbitTemplate.convertAndSend(RabbitMQTestConfig.create_user_exchange, "create.user", userMessageDto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQTestConfig.create_user_queue).getMessageCount() == 0);
        Thread.sleep(Duration.of(2, ChronoUnit.SECONDS));
        // update
        userMessageDto.setUsername("UPDATED");
        rabbitTemplate.convertAndSend(RabbitMQTestConfig.update_user_exchange, "update.user", userMessageDto);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQTestConfig.update_user_queue).getMessageCount() == 0);
        Thread.sleep(Duration.of(2, ChronoUnit.SECONDS));
        // then
        assertEquals(profileRepository.findById(profileId).get().getName(), userMessageDto.getName());
        assertEquals(profileRepository.findById(profileId).get().getUsername(), "UPDATED");
        assertTrue(redisTemplate.hasKey("profile::" + SHA256.hash(profileId.toString())));
    }


}
