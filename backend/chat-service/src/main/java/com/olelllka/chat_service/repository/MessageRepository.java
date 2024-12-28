package com.olelllka.chat_service.repository;

import com.olelllka.chat_service.domain.entity.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends MongoRepository<MessageEntity, String> {

    @Query(value = "{ 'chatId' : ?0 }", sort = "{ 'createdAt': -1 }")
    Page<MessageEntity> findByChatId(String chatId, Pageable pageable);
}
