package com.olelllka.feed_service.service;

import com.olelllka.feed_service.RabbitMQConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(RabbitMQConfiguration.class)
public class RabbitListenerIntegrationTest {

    @ServiceConnection
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.6")).withExposedPorts(6379);

    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.13-management"));

    private final RedisTemplate<String, String> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;

    @Autowired
    public RabbitListenerIntegrationTest(RedisTemplate<String, String> redisTemplate,
                                         RabbitTemplate rabbitTemplate,
                                         RabbitAdmin rabbitAdmin) {
        this.redisTemplate = redisTemplate;
        this.rabbitAdmin = rabbitAdmin;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Test
    public void testThatRabbitListenerHandlesProfileDeletionCorrectly() {
        UUID profileId = UUID.randomUUID();
        String postId = UUID.randomUUID().toString(); // for the sake of example

        redisTemplate.opsForList().leftPush("feed:profile:"+SHA256.hash(profileId.toString()), postId);
        assertTrue(redisTemplate.hasKey("feed:profile:"+SHA256.hash(profileId.toString())));

        rabbitTemplate.convertAndSend(RabbitMQConfiguration.fanoutExchange, "", profileId);
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> rabbitAdmin.getQueueInfo(RabbitMQConfiguration.delete_queue).getMessageCount() == 0);

        assertFalse(redisTemplate.hasKey("feed:profile:"+SHA256.hash(profileId.toString())));
    }

}
