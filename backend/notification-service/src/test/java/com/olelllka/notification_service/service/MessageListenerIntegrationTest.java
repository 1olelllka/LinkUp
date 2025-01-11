package com.olelllka.notification_service.service;

import com.olelllka.notification_service.RabbitMQTestConfig;
import com.olelllka.notification_service.TestDataUtil;
import com.olelllka.notification_service.domain.dto.NotificationDto;
import com.olelllka.notification_service.repository.NotificationRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    private final NotificationRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin admin;

    @Autowired
    public MessageListenerIntegrationTest(NotificationRepository repository,
                                          RabbitAdmin admin,
                                          RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.admin = admin;
    }

    @AfterAll
    static void tearDown() {
        mongoDBContainer.stop();
        mongoDBContainer.close();
        rabbitMQContainer.stop();
        rabbitMQContainer.close();
    }

    @Test
    public void testThatMessageListenerPerformsWellOnIncomingMessage() throws Exception {
        // mock another services
        ObjectMapper objectMapper = new ObjectMapper();
        NotificationDto dto = TestDataUtil.createNotificationDto();
        rabbitTemplate.convertAndSend("notification_exchange", "notifications", objectMapper.writeValueAsString(dto));
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> admin.getQueueInfo("notification_queue").getMessageCount() == 0);
        // test
        Pageable pageable = PageRequest.of(0, 1);
        assertEquals(repository.findByUserId(UUID.fromString(dto.getUserId()), pageable).getTotalElements(), 1);
    }
}
