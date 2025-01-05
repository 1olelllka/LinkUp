package com.olelllka.chat_service.repository;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatRepository extends MongoRepository<ChatEntity, String> {

    @Query("{ 'participants': ?0 }")
    Page<ChatEntity> findChatsByUserId(UUID userId, Pageable pageable);

    @Query("{ 'participants' : { $size: 2, $all: [?0, ?1] } }")
    Optional<ChatEntity> findChatByTwoMembers(UUID userId1, UUID userId2);
}
