package com.olelllka.chat_service.service;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.repository.ChatRepository;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceUnitTest {

    @Mock
    private ChatRepository chatRepository;
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
}
