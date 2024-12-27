package com.olelllka.chat_service.repository;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.TestcontainersConfiguration;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.MongoDBContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Import(TestcontainersConfiguration.class)
public class ChatRepositoryMongoDataTest {

    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

    @Autowired
    private ChatRepository repository;

    static {
        mongo.start();
    }

    @Test
    public void testThatRepositoryFindsTheRightChatById() {
        // given
        ChatEntity chat = TestDataUtil.createChatEntity(List.of());
        repository.save(chat);
        Pageable pageable = PageRequest.of(0, 1);
        Page<ChatEntity> expected = new PageImpl<>(List.of(chat));
        // when
        Page<ChatEntity> result = repository.findChatsByUserId("1234", pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().size(), expected.getContent().size())
        );
    }

    @Test
    public void testThatRepositoryFindsNothing() {
        // given
        ChatEntity chat = TestDataUtil.createChatEntity(List.of());
        repository.save(chat);
        Pageable pageable = PageRequest.of(0, 1);
        // when
        Page<ChatEntity> result = repository.findChatsByUserId("1235", pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().size(), 0)
        );
    }

    @Test
    public void testThatRepositoryFindsTheSpecificChatWithOnlyOneMessage() {
        // given
        MessageEntity msg1 = TestDataUtil.createMessageEntity();
        msg1.setIdIfNotPresent();
        MessageEntity msg2 = TestDataUtil.createMessageEntity();
        msg2.setIdIfNotPresent();
        ChatEntity chat = TestDataUtil.createChatEntity(List.of(msg1, msg2));
        repository.save(chat);
        // when
        Optional<ChatEntity> result = repository.findChatByMessageId(msg2.getId());
        // then
        assertAll(
                () -> assertFalse(result.isEmpty()),
                () -> assertEquals(result.get().getMessages().getFirst().getId(), msg2.getId())
        );
    }

    @Test
    public void testThatRepositoryCannotFindSpecificChatWithMsgId() {
        // given
        MessageEntity msg1 = TestDataUtil.createMessageEntity();
        msg1.setIdIfNotPresent();
        MessageEntity msg2 = TestDataUtil.createMessageEntity();
        msg2.setIdIfNotPresent();
        ChatEntity chat = TestDataUtil.createChatEntity(List.of(msg1));
        repository.save(chat);
        // when
        Optional<ChatEntity> result = repository.findChatByMessageId(msg2.getId());
        // then
        assertAll(
                () -> assertTrue(result.isEmpty())
        );
    }
}
