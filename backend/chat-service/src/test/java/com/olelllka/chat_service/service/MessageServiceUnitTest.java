package com.olelllka.chat_service.service;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceUnitTest {

    @Mock
    private MessageRepository messageRepository;
    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    public void testThatServiceGetsAllOfTheMessagesBySpecificChat() {
        // given
        Pageable pageable = PageRequest.of(0, 1);
        Page<MessageEntity> expected = new PageImpl<>(List.of(TestDataUtil.createMessageEntity("12345"), TestDataUtil.createMessageEntity("12345")));
        // when
        when(messageRepository.findByChatId("12345", pageable)).thenReturn(expected);
        Page<MessageEntity> result = messageService.getMessagesForChat("12345", pageable);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), expected.getTotalElements())
        );
    }

    @Test
    public void testThatByUpdateThrowsException() {
        // given
        MessageDto dto = TestDataUtil.createMessageDto("12345");
        String id = "12345678";
        // when
        when(messageRepository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> messageService.updateMessage(id, dto));
        verify(messageRepository, never()).save(any(MessageEntity.class));
    }

    @Test
    public void testThatByUpdateItUpdatesContent() {
        // given
        MessageDto dto = TestDataUtil.createMessageDto("12345");
        dto.setContent("UPDATED");
        String id = "12345678";
        MessageEntity expected = TestDataUtil.createMessageEntity("12345");
        expected.setContent("UPDATED");
        // when
        when(messageRepository.findById(id)).thenReturn(Optional.of(TestDataUtil.createMessageEntity("12345")));
        when(messageRepository.save(expected)).thenReturn(expected);
        MessageEntity result = messageService.updateMessage(id, dto);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent(), expected.getContent())
        );
    }

    @Test
    public void testThatServiceDeletesMessage() {
        // given
        String id = "1324";
        // when
        messageService.deleteSpecificMessage(id);
        // then
        verify(messageRepository, times(1)).deleteById(id);
    }

}
