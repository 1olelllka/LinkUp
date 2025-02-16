package com.olelllka.chat_service.service;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.feign.ProfileFeign;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceUnitTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private ProfileFeign profileFeign;
    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    public void testThatGetChatsByUserIdWorks() {
        // given
        Page<ChatEntity> expected = new PageImpl<>(List.of(TestDataUtil.createChatEntity()));
        Pageable pageable = PageRequest.of(0, 1);
        UUID userId = UUID.randomUUID();
        // when
        when(chatRepository.findChatsByUserId(userId, pageable)).thenReturn(expected);
        Page<ChatEntity> result = chatService.getChatsForUser(userId, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent().get(0).getParticipants()[0], expected.getContent().get(0).getParticipants()[0]),
                () -> assertEquals(result.getContent().get(0).getParticipants()[1], expected.getContent().get(0).getParticipants()[1])
        );
        verify(chatRepository, times(1)).findChatsByUserId(userId, pageable);
    }

    @Test
    public void testThatCreateNewChatWorks() {
        // given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        ChatEntity expected = TestDataUtil.createChatEntity();
        UUID[] ids = {userId1, userId2};
        expected.setParticipants(ids);
        // when
        when(profileFeign.getProfileById(userId1)).thenReturn(ResponseEntity.ok().build());
        when(profileFeign.getProfileById(userId2)).thenReturn(ResponseEntity.ok().build());
        when(chatRepository.save(expected)).thenReturn(expected);
        ChatEntity result = chatService.createNewChat(userId1, userId2);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getParticipants()[0], expected.getParticipants()[0]),
                () -> assertEquals(result.getParticipants()[1], expected.getParticipants()[1])
        );
    }

    @Test
    public void testThatCreateNewChatThrowsExceptionIfOneOfTheUsersDoesNotExist() {
        // given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        // when
        when(profileFeign.getProfileById(userId1)).thenReturn(ResponseEntity.notFound().build());
        // then
        assertThrows(NotFoundException.class, () -> chatService.createNewChat(userId1, userId2));
        verify(chatRepository, never()).save(any(ChatEntity.class));
    }

    @Test
    public void testThatDeleteChatWorks() {
        // given
        String chatId = "1235";
        Query query = new Query();
        query.addCriteria(Criteria.where("chatId").is(chatId));
        // when
        chatService.deleteChat(chatId);
        // then
        verify(chatRepository, times(1)).deleteById(chatId);
        verify(mongoTemplate, times(1)).findAllAndRemove(query, MessageEntity.class, "Message");
    }
}
