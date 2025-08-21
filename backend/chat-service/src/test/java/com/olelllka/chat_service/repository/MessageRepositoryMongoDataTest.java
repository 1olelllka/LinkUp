package com.olelllka.chat_service.repository;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import lombok.extern.java.Log;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Log
@DataMongoTest
public class MessageRepositoryMongoDataTest {

    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8.0");

    static {
        mongo.start();
    }

    @AfterAll
    static void tearDown() {
        mongo.stop();
        mongo.close();
    }

    @Autowired
    private MessageRepository repository;

    @Test
    public void testThatRepositoryFindsPageOfMessagesSortedByDate() {
        // given
        MessageEntity msg1 = TestDataUtil.createMessageEntity("12345");
        MessageEntity msg2 = TestDataUtil.createMessageEntity("12345");
        msg2.setCreatedAt(new Date());
        msg2.setContent("New Msg");
        repository.save(msg1);
        repository.save(msg2);
        Pageable pageable = PageRequest.of(0, 2);
        // when
        Page<MessageEntity> result = repository.findByChatId("12345", pageable);
        log.info(result.getContent() + "");
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertNotEquals("New Msg", result.getContent().get(1).getContent())
        );
    }

}
