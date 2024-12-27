package com.olelllka.chat_service.service;

import com.mongodb.client.result.UpdateResult;
import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.service.impl.ChatServiceImpl;
import org.bson.types.ObjectId;
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
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceUnitTest {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private MongoTemplate mongoTemplate;
    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    public void testThatGetChatsByUserIdWorks() {
        // given
        Page<ChatEntity> expected = new PageImpl<>(List.of(TestDataUtil.createChatEntity(List.of())));
        Pageable pageable = PageRequest.of(0, 1);
        String userId = "1234";
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
        String userId1 = "1234";
        String userId2 = "5678";
        ChatEntity expected = TestDataUtil.createChatEntity(List.of());
        // when
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
    public void testThatDeleteChatWorks() {
        // given
        String chatId = "1235";
        // when
        chatService.deleteChat(chatId);
        // then
        verify(chatRepository, times(1)).deleteById(chatId);
    }

    @Test
    public void testThatGetAllMessagesByChatIdThrowsException() {
        // given
        String chatId = "12345";
        Pageable pageable = PageRequest.of(0, 1);
        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> chatService.getMessagesForChat(chatId, pageable));
    }

    @Test
    public void testThatGetAllMessagesByChatIdReturnsPageOfMessages() {
        // given
        String chatId = "12345";
        MessageEntity msg = TestDataUtil.createMessageEntity();
        Pageable pageable = PageRequest.of(0, 1);
        Page<MessageEntity> expected = new PageImpl<>(List.of(msg));
        ChatEntity chat = TestDataUtil.createChatEntity(List.of(msg));
        // when
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        Page<MessageEntity> result = chatService.getMessagesForChat(chatId, pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), expected.getTotalElements()),
                () -> assertEquals(result.getContent().get(0).getContent(), expected.getContent().get(0).getContent())
        );
        verify(chatRepository, times(1)).findById(chatId);
    }

    @Test
    public void testThatUpdateMessageThrowsException() {
        // given
        String chat_id = "123534123541234512345120";
        String msg_id = "678951245614342341234123";
        MessageDto dto = TestDataUtil.createMessageDto();
        Query query = new Query(Criteria.where("_id").is(new ObjectId(chat_id)).and("messages.id").is(new ObjectId(msg_id)));
        Update update = new Update().set("messages.$.content",dto.getContent());
        UpdateResult updateResult = UpdateResult.acknowledged(0, 0L, null);
        // when
        when(mongoTemplate.updateFirst(query, update, ChatEntity.class)).thenReturn(updateResult);
        // then
        assertThrows(NotFoundException.class, () -> chatService.updateMessage(chat_id, msg_id, dto));
        verify(mongoTemplate, never()).findOne(any(Query.class), eq(ChatEntity.class));
    }

    @Test
    public void testThatUpdateMessageReturnsUpdatedMessage() {
        // given
        String chat_id = "123534123541234512345120";
        String msg_id = "678951245614342341234123";
        MessageDto dto = TestDataUtil.createMessageDto();
        dto.setContent("UPDATED");
        Query query = new Query(Criteria.where("_id").is(new ObjectId(chat_id)).and("messages.id").is(new ObjectId(msg_id)));
        Update update = new Update().set("messages.$.content",dto.getContent());
        UpdateResult updateResult = UpdateResult.acknowledged(1, 1L, null);
        Query findQuery = new Query(Criteria.where("_id").is(chat_id).and("messages.id").is(msg_id));
        findQuery.fields().include("messages.$");
        MessageEntity updatedMessage = TestDataUtil.createMessageEntity();
        updatedMessage.setContent("UPDATED");
        ChatEntity expected = TestDataUtil.createChatEntity(List.of(updatedMessage));
        // when
        when(mongoTemplate.updateFirst(query, update, ChatEntity.class)).thenReturn(updateResult);
        when(mongoTemplate.findOne(findQuery, ChatEntity.class)).thenReturn(expected);
        MessageEntity message = chatService.updateMessage(chat_id, msg_id, dto);
        // then
        assertAll(
                () -> assertNotNull(message),
                () -> assertEquals(expected.getMessages().getFirst().getContent(), message.getContent())
        );
    }

    @Test
    public void testThatDeleteMessageWorksWell() {
        String chatId = "123534123541234512345120";
        String msgId = "678951245614342341234123";
        Query query = new Query(Criteria.where("_id").is(new ObjectId(chatId)).and("messages.id").is(new ObjectId(msgId)));
        Update update = new Update().pull("messages", new Query(Criteria.where("id").is(new ObjectId(msgId))));
        // when
        chatService.deleteSpecificMessage(chatId, msgId);
        // then
        verify(mongoTemplate, times(1)).updateFirst(query, update, ChatEntity.class);
    }
}
