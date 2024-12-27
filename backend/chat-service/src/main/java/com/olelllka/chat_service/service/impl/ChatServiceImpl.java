package com.olelllka.chat_service.service.impl;

import com.mongodb.client.result.UpdateResult;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.service.ChatService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRepository repository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<ChatEntity> getChatsForUser(String userId, Pageable pageable) {
        return repository.findChatsByUserId(userId, pageable);
    }

    @Override
    public ChatEntity createNewChat(String userId1, String userId2) {
        ChatEntity newChat = ChatEntity.builder()
                .participants(new String[]{userId1, userId2})
                .messages(List.of())
                .build();
        return repository.save(newChat);
    }

    @Override
    public void deleteChat(String chatId) {
        repository.deleteById(chatId);
    }

    @Override
    public Page<MessageEntity> getMessagesForChat(String chatId, Pageable pageable) {
        ChatEntity chat = repository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat with such id was not found."));
        return new PageImpl<>(chat.getMessages(), pageable, chat.getMessages().size());
    }

    @Override
    public MessageEntity updateMessage(String chat_id, String msg_id, MessageDto updatedMsg) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(chat_id)).and("messages.id").is(new ObjectId(msg_id)));
        Update update = new Update().set("messages.$.content", updatedMsg.getContent());
        UpdateResult result = mongoTemplate.updateFirst(query, update, ChatEntity.class);
        if (result.getModifiedCount() == 0) {
            throw new NotFoundException("Message with such id was not found.");
        }
        ChatEntity chatWithUpdatedMessage = repository.findChatByMessageId(msg_id).orElseThrow(() -> new NotFoundException("Message with such id was not found."));
        return chatWithUpdatedMessage.getMessages().getFirst();
    }

    @Override
    public void deleteSpecificMessage(String chatId, String msgId) {
        Query query = new Query(Criteria.where("_id").is(new ObjectId(chatId)).and("messages.id").is(new ObjectId(msgId)));
        Update update = new Update().pull("messages", new Query(Criteria.where("id").is(new ObjectId(msgId))));
        mongoTemplate.updateFirst(query, update, ChatEntity.class);
    }
}
