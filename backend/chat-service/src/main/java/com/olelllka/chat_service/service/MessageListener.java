package com.olelllka.chat_service.service;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageListener {

    private final MongoTemplate mongoTemplate;

    @RabbitListener(queues = "delete_profile_queue_chat")
    void handleClearingChatsForDeletedUser(UUID userId) {
        Query chatQuery = new Query();
        chatQuery.addCriteria(Criteria.where("participants.id").is(userId));
        mongoTemplate.findAllAndRemove(chatQuery, ChatEntity.class, "Chat");

        Query messageQuery = new Query();
        messageQuery.addCriteria(Criteria.where("from").is(userId));
        mongoTemplate.findAllAndRemove(messageQuery, MessageEntity.class, "Message");
        messageQuery = new Query();
        messageQuery.addCriteria(Criteria.where("to").is(userId));
        mongoTemplate.findAllAndRemove(messageQuery, MessageEntity.class, "Message");
    }

}
