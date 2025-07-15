package com.olelllka.chat_service.repository;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.MongoDBContainer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
public class ChatRepositoryMongoDataTest {

    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

    @Autowired
    private ChatRepository repository;

    static {
        mongo.start();
    }

    @AfterAll
    static void tearDown() {
        mongo.stop();
        mongo.close();
    }

    @Test
    public void testThatRepositoryFindsTheRightChatById() {
        // given
        ChatEntity chat = TestDataUtil.createChatEntity();
        ChatEntity saved = repository.save(chat);
        Pageable pageable = PageRequest.of(0, 1);
        Page<ChatEntity> expected = new PageImpl<>(List.of(chat));
        // when
        Page<ChatEntity> result = repository.findChatsByUserId(saved.getParticipants()[0].getId(), pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().size(), expected.getContent().size())
        );
    }

    @Test
    public void testThatRepositoryFindsNothing() {
        // given
        ChatEntity chat = TestDataUtil.createChatEntity();
        repository.save(chat);
        Pageable pageable = PageRequest.of(0, 1);
        // when
        Page<ChatEntity> result = repository.findChatsByUserId(UUID.randomUUID(), pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().size(), 0)
        );
    }

    @Test
    public void testThatRepositoryFindsTheRightChatByTwoUsers() {
        // given
        ChatEntity chat = TestDataUtil.createChatEntity();
        ChatEntity expected = repository.save(chat);
        // when
        Optional<ChatEntity> result = repository.findChatByTwoMembers(chat.getParticipants()[0].getId(), chat.getParticipants()[1].getId());
        // then
        assertAll(
                () -> assertTrue(result.isPresent()),
                () -> assertEquals(result.get().getId(), expected.getId())
        );
    }

    @Test
    public void testThatRepositoryDoesNotFindAnyChatByTwoUsers() {
        // given
        ChatEntity chat = TestDataUtil.createChatEntity();
        repository.save(chat);
        // when
        Optional<ChatEntity> result = repository.findChatByTwoMembers(UUID.randomUUID(), UUID.randomUUID());
        // then
        assertAll(
                () -> assertTrue(result.isEmpty())
        );
    }
}
