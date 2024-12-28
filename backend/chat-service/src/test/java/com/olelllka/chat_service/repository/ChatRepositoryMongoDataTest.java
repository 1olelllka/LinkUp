package com.olelllka.chat_service.repository;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.TestcontainersConfiguration;
import com.olelllka.chat_service.domain.entity.ChatEntity;
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
        ChatEntity chat = TestDataUtil.createChatEntity();
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
        ChatEntity chat = TestDataUtil.createChatEntity();
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
    public void testThatRepositoryFindsTheRightChatByTwoUsers() {
        // given
        ChatEntity chat = TestDataUtil.createChatEntity();
        ChatEntity expected = repository.save(chat);
        // when
        Optional<ChatEntity> result = repository.findChatByTwoMembers(chat.getParticipants()[0], chat.getParticipants()[1]);
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
        ChatEntity expected = repository.save(chat);
        // when
        Optional<ChatEntity> result = repository.findChatByTwoMembers("12345", "76543");
        // then
        assertAll(
                () -> assertTrue(result.isEmpty())
        );
    }
}
