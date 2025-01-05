package com.olelllka.chat_service.mapper;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.dto.ChatDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.mapper.impl.ChatMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ChatMapperUnitTest {

    @InjectMocks
    private ChatMapperImpl chatMapper;

    @Test
    public void testThatEntityMapsToDto() {
        // given
        ChatEntity given = TestDataUtil.createChatEntity();
        ChatDto expected = TestDataUtil.createChatDto();
        expected.setParticipants(given.getParticipants());
        // when
        ChatDto result = chatMapper.toDto(given);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getParticipants()[0], expected.getParticipants()[0]),
                () -> assertEquals(result.getParticipants()[1], expected.getParticipants()[1])
        );
    }

    @Test
    public void testThatDtoMapsToEntity() {
        // given
        ChatEntity expected = TestDataUtil.createChatEntity();
        ChatDto given = TestDataUtil.createChatDto();
        expected.setParticipants(given.getParticipants());
        // when
        ChatEntity result = chatMapper.toEntity(given);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getParticipants()[0], expected.getParticipants()[0]),
                () -> assertEquals(result.getParticipants()[1], expected.getParticipants()[1])
        );
    }
}
