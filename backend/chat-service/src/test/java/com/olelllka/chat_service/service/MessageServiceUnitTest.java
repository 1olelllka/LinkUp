package com.olelllka.chat_service.service;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.rest.exception.AuthException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceUnitTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private JWTUtil jwtUtil;
    @InjectMocks
    private MessageServiceImpl messageService;

    @Test
    public void testThatServiceGetsAllOfTheMessagesBySpecificChat() {
        // given
        Pageable pageable = PageRequest.of(0, 1);
        Page<MessageEntity> expected = new PageImpl<>(List.of(TestDataUtil.createMessageEntity("12345"), TestDataUtil.createMessageEntity("12345")));
        String jwt = "jwt";
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(expected.getContent().getFirst().getFrom().toString());
        when(messageRepository.findByChatId("12345", pageable)).thenReturn(expected);
        Page<MessageEntity> result = messageService.getMessagesForChat("12345", pageable, jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getTotalElements(), expected.getTotalElements())
        );
    }

    @Test
    public void testThatServiceGetAllOfTheMessagesBySpecificChatThrowsException() {
        // given
        Pageable pageable = PageRequest.of(0, 1);
        Page<MessageEntity> expected = new PageImpl<>(List.of(TestDataUtil.createMessageEntity("12345"), TestDataUtil.createMessageEntity("12345")));
        String jwt = "jwt";
        // when
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        when(messageRepository.findByChatId("12345", pageable)).thenReturn(expected);
        // then
        assertThrows(AuthException.class, () -> messageService.getMessagesForChat("12345", pageable, jwt));
    }

    @Test
    public void testThatByUpdateThrowsNotFoundException() {
        // given
        MessageDto dto = TestDataUtil.createMessageDto("12345");
        String id = "12345678";
        // when
        when(messageRepository.findById(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> messageService.updateMessage(id, dto, "jwt"));
        verify(messageRepository, never()).save(any(MessageEntity.class));
    }

    @Test
    public void testThatByUpdateThrowsAuthException() {
        // given
        MessageDto dto = TestDataUtil.createMessageDto("12345");
        String id = "12345678";
        String jwt = "jwt";
        // when
        when(messageRepository.findById(id)).thenReturn(Optional.of(TestDataUtil.createMessageEntity("12345")));
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        // then
        assertThrows(AuthException.class, () -> messageService.updateMessage(id, dto, "jwt"));
        verify(messageRepository, never()).save(any(MessageEntity.class));
    }

    @Test
    public void testThatByUpdateItUpdatesContent() {
        // given
        MessageDto dto = TestDataUtil.createMessageDto("12345");
        dto.setContent("UPDATED");
        String id = "12345678";
        MessageEntity expected = TestDataUtil.createMessageEntity("12345");
        String jwt = "jwt";
        expected.setContent("UPDATED");
        expected.setTo(dto.getTo());
        expected.setFrom(dto.getFrom());
        // when
        when(messageRepository.findById(id)).thenReturn(Optional.of(expected));
        when(messageRepository.save(expected)).thenReturn(expected);
        when(jwtUtil.extractId(jwt)).thenReturn(expected.getFrom().toString());
        MessageEntity result = messageService.updateMessage(id, dto, jwt);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getContent(), expected.getContent())
        );
    }

    @Test
    public void testThatServiceDeletesMessageIfMessageDoesNotExist() {
        // given
        String id = "1324";
        String jwt = "jwt";
        // when
        when(messageRepository.existsById(id)).thenReturn(false);
        messageService.deleteSpecificMessage(id, jwt);
        // then
        verify(messageRepository, times(1)).deleteById(id);
        verify(messageRepository, never()).findById(anyString());
        verify(jwtUtil, never()).extractId(anyString());
    }

    @Test
    public void testThatServiceDeletesMessageIfMessageExists() {
        // given
        String id = "1324";
        String jwt = "jwt";
        MessageEntity msg = TestDataUtil.createMessageEntity("12356");
        // when
        when(messageRepository.existsById(id)).thenReturn(true);
        when(messageRepository.findById(id)).thenReturn(Optional.of(msg));
        when(jwtUtil.extractId(jwt)).thenReturn(msg.getFrom().toString());
        messageService.deleteSpecificMessage(id, jwt);
        // then
        verify(messageRepository, times(1)).deleteById(id);
        verify(messageRepository, times(1)).findById(id);
        verify(jwtUtil, times(1)).extractId(jwt);
    }

    @Test
    public void testThatDeleteMessageThrowsAuthException() {
        String id = "1324";
        String jwt = "jwt";
        MessageEntity msg = TestDataUtil.createMessageEntity("12356");
        // when
        when(messageRepository.existsById(id)).thenReturn(true);
        when(messageRepository.findById(id)).thenReturn(Optional.of(msg));
        when(jwtUtil.extractId(jwt)).thenReturn(UUID.randomUUID().toString());
        // then
        assertThrows(AuthException.class, () -> messageService.deleteSpecificMessage(id, jwt));
        verify(messageRepository, never()).deleteById(id);
        verify(messageRepository, times(1)).findById(id);
        verify(jwtUtil, times(1)).extractId(jwt);
    }

}
