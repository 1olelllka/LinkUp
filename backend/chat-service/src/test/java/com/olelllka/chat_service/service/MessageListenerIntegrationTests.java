package com.olelllka.chat_service.service;

import com.olelllka.chat_service.RabbitMQTestConfig;
import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(RabbitMQTestConfig.class)
public class MessageListenerIntegrationTests {

    @ServiceConnection
    static RabbitMQContainer rabbitMq = new RabbitMQContainer("rabbitmq:3.13-management");

    @ServiceConnection
    static MongoDBContainer mongoDb = new MongoDBContainer("mongo:8.0");

    static {
        rabbitMq.start();
        mongoDb.start();
    }

    @AfterAll
    static void tearDown() {
        rabbitMq.stop();
        rabbitMq.close();
        mongoDb.stop();
        mongoDb.close();
    }

    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    @Autowired
    public MessageListenerIntegrationTests(RabbitAdmin rabbitAdmin,
                                           ChatRepository chatRepository,
                                           MessageRepository messageRepository,
                                           RabbitTemplate rabbitTemplate) {
        this.rabbitAdmin = rabbitAdmin;
        this.chatRepository = chatRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.messageRepository = messageRepository;
    }

    @Test
    public void testThatProfileDeletionHandlingWorksFine() {
        ChatEntity chat = chatRepository.save(TestDataUtil.createChatEntity());
        MessageEntity msg1 = TestDataUtil.createMessageEntity(chat.getId());
        msg1.setFrom(chat.getParticipants()[0].getId());
        messageRepository.save(msg1);
        MessageEntity msg2 = TestDataUtil.createMessageEntity(chat.getId());
        msg2.setTo(chat.getParticipants()[0].getId());
        messageRepository.save(msg2);
        assertAll(
                () -> assertEquals(1, chatRepository.count()),
                () -> assertEquals(2, messageRepository.count())
        );
        rabbitTemplate.convertAndSend(RabbitMQTestConfig.profile_delete_exchange, "", chat.getParticipants()[0].getId());
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> rabbitAdmin.getQueueInfo(RabbitMQTestConfig.delete_queue_chat).getMessageCount() == 0);

        assertAll(
                () -> assertEquals(0, chatRepository.count()),
                () -> assertEquals(0, messageRepository.count())
        );
    }
}
