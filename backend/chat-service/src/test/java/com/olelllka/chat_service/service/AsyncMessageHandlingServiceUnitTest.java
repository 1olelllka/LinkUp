package com.olelllka.chat_service.service;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AsyncMessageHandlingServiceUnitTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ChatRepository chatRepository;
    @InjectMocks
    private AsyncMessageHandlingService messageHandlingService;

    @Test
    public void testThatSaveMessageToDatabaseThrowsExceptionIfChatWasNotFound() {
        MessageEntity entity = TestDataUtil.createMessageEntity("12356");

        when(chatRepository.findById(entity.getChatId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> messageHandlingService.saveMessageToDatabase(entity));
        verify(messageRepository, never()).save(any(MessageEntity.class));
        verify(chatRepository, never()).save(any(ChatEntity.class));
    }

    @Test
    public void testThatSaveMessageToDatabaseWorks() {
        ChatEntity chat = TestDataUtil.createChatEntity();
        chat.setId("123456");
        MessageEntity entity = TestDataUtil.createMessageEntity("12356");
        when(chatRepository.findById(entity.getChatId())).thenReturn(Optional.of(chat));
        when(chatRepository.save(chat)).thenReturn(chat);

        assertDoesNotThrow(() -> messageHandlingService.saveMessageToDatabase(entity));
        verify(messageRepository, times(1)).save(entity);
        verify(chatRepository, times(1)).save(chat);
    }

    @Test
    public void testThatUpdateChatsLastMessageInDatabaseThrowsException() {
        MessageEntity entity = TestDataUtil.createMessageEntity("12356");

        when(chatRepository.findById(entity.getChatId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> messageHandlingService.updateChatsLastMessageInDatabase(entity));
        verify(chatRepository, never()).save(any(ChatEntity.class));
    }

    @Test
    public void testThatUpdateChatsLastMessageInDatabaseWorksIfItIsTheLastMsg() {
        ChatEntity chat = TestDataUtil.createChatEntity();
        chat.setId("123456");
        MessageEntity entity = TestDataUtil.createMessageEntity("12356");
        when(chatRepository.findById(entity.getChatId())).thenReturn(Optional.of(chat));
        when(messageRepository.findByChatId(chat.getId(), PageRequest.of(0, 1)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(chatRepository.save(chat)).thenReturn(chat);
        assertDoesNotThrow(() -> messageHandlingService.updateChatsLastMessageInDatabase(entity));
        verify(chatRepository, times(1)).save(chat);
    }

    @Test
    public void testThatUpdateChatsLastMessageInDatabaseWorksIfItIsNotTheLastMsg() {
        ChatEntity chat = TestDataUtil.createChatEntity();
        chat.setId("123456");
        MessageEntity entity = TestDataUtil.createMessageEntity("12356");
        when(chatRepository.findById(entity.getChatId())).thenReturn(Optional.of(chat));
        when(messageRepository.findByChatId(chat.getId(), PageRequest.of(0, 1)))
                .thenReturn(new PageImpl<>(List.of()));
        when(chatRepository.save(chat)).thenReturn(chat);

        assertDoesNotThrow(() -> messageHandlingService.updateChatsLastMessageInDatabase(entity));
        verify(chatRepository, times(1)).save(chat);
    }

    @Test
    public void testThatDeleteMessageFromDatabaseWorks() {
        ChatEntity chat = TestDataUtil.createChatEntity();
        chat.setId("123456");
        MessageEntity entity = TestDataUtil.createMessageEntity("12356");
        when(chatRepository.findById(entity.getChatId())).thenReturn(Optional.of(chat));
        chat.setLastMessage("*The message was deleted*");
        when(chatRepository.save(chat)).thenReturn(chat);

        assertDoesNotThrow(() -> messageHandlingService.deleteMessageFromDatabase(entity));
        verify(messageRepository, times(1)).deleteById(entity.getId());
        verify(chatRepository, times(1)).save(chat);
    }

}
