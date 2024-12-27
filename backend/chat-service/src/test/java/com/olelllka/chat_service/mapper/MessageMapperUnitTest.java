package com.olelllka.chat_service.mapper;

import com.olelllka.chat_service.TestDataUtil;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.mapper.impl.MessageMapperImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MessageMapperUnitTest {

    @InjectMocks
    private MessageMapperImpl mapper;

    @Test
    public void testThatEntityMapsToDto() {
        // given
        MessageEntity given = TestDataUtil.createMessageEntity();
        MessageDto expected = TestDataUtil.createMessageDto();
        // when
        MessageDto result = mapper.toDto(given);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getFrom(), expected.getFrom()),
                () -> assertEquals(result.getTo(), expected.getTo()),
                () -> assertEquals(result.getContent(), expected.getContent())
        );
    }

    @Test
    public void testThatDtoMapsToEntity() {
        // given
        MessageDto given = TestDataUtil.createMessageDto();
        MessageEntity expected = TestDataUtil.createMessageEntity();
        // when
        MessageEntity result = mapper.toEntity(given);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getFrom(), expected.getFrom()),
                () -> assertEquals(result.getTo(), expected.getTo()),
                () -> assertEquals(result.getContent(), expected.getContent())
        );
    }
}
