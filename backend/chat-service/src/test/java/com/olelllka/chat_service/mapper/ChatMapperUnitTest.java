package com.olelllka.chat_service.mapper;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.dto.ChatDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.mapper.impl.ChatMapperImpl;
import com.olelllka.chat_service.mapper.impl.MessageMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatMapperUnitTest {

    @Mock
    private MessageMapperImpl messageMapper;
    @InjectMocks
    private ChatMapperImpl chatMapper;

    @Test
    public void testThatEntityMapsToDto() {
        // given
        ChatEntity given = TestDataUtil.createChatEntity(List.of(TestDataUtil.createMessageEntity()));
        ChatDto expected = TestDataUtil.createChatDto(List.of(TestDataUtil.createMessageDto()));
        // when
        when(messageMapper.toDto(TestDataUtil.createMessageEntity())).thenReturn(TestDataUtil.createMessageDto());
        ChatDto result = chatMapper.toDto(given);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getMessages().get(0).getContent(), expected.getMessages().get(0).getContent()),
                () -> assertEquals(result.getParticipants()[0], expected.getParticipants()[0]),
                () -> assertEquals(result.getParticipants()[1], expected.getParticipants()[1])
        );
        verify(messageMapper, times(1)).toDto(TestDataUtil.createMessageEntity());
    }

    @Test
    public void testThatDtoMapsToEntity() {
        // given
        ChatEntity expected = TestDataUtil.createChatEntity(List.of(TestDataUtil.createMessageEntity()));
        ChatDto given = TestDataUtil.createChatDto(List.of(TestDataUtil.createMessageDto()));
        // when
        when(messageMapper.toEntity(TestDataUtil.createMessageDto())).thenReturn(TestDataUtil.createMessageEntity());
        ChatEntity result = chatMapper.toEntity(given);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getMessages().get(0).getContent(), expected.getMessages().get(0).getContent()),
                () -> assertEquals(result.getParticipants()[0], expected.getParticipants()[0]),
                () -> assertEquals(result.getParticipants()[1], expected.getParticipants()[1])
        );
        verify(messageMapper, times(1)).toEntity(TestDataUtil.createMessageDto());
    }
}
