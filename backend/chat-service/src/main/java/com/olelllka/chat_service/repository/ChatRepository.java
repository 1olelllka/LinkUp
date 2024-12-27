package com.olelllka.chat_service.repository;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends MongoRepository<ChatEntity, String> {

    @Query("{ 'participants': ?0 }")
    Page<ChatEntity> findChatsByUserId(String userId, Pageable pageable);
}
