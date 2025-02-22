package com.olelllka.auth_service.service;

import com.olelllka.auth_service.RabbitMQTestConfig;
import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(RabbitMQTestConfig.class)
public class MessageListenerIntegrationTest {

    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:8.0"));

    static {
        rabbitMQContainer.start();
        mongoDBContainer.start();
    }

    @AfterAll
    static void tearDown() {
        rabbitMQContainer.stop();
        rabbitMQContainer.close();
        mongoDBContainer.stop();
        mongoDBContainer.close();
    }

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin admin;

    @Autowired
    public MessageListenerIntegrationTest(UserRepository userRepository,
                                          RabbitTemplate rabbitTemplate,
                                          RabbitAdmin admin) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.admin = admin;
    }

    @Test
    public void testThatListenerHandlesDeleteProfileCorrectly() {
        UUID profileId = UUID.randomUUID();
        UserEntity user = TestDataUtil.createUserEntity();
        user.setUserId(profileId);
        userRepository.save(user);
        assertTrue(userRepository.existsById(profileId));
        rabbitTemplate.convertAndSend(RabbitMQTestConfig.profile_fanout_exchange, "", profileId);
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> admin.getQueueInfo(RabbitMQTestConfig.delete_queue_auth).getMessageCount() == 0);
        assertFalse(userRepository.existsById(profileId));
    }

}
