package com.olelllka.profile_service.service;

import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.dto.ProfileDocumentDto;
import com.olelllka.profile_service.domain.entity.ProfileDocument;
import com.olelllka.profile_service.repository.ProfileDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageListenerUnitTest {

    @Mock
    private ProfileDocumentRepository repository;
    @InjectMocks
    private MessageListener messageListener;

    @Test
    public void testThatCreateUpdateProfileOnElasticsearchSavesRightValues() {
        // given
        ProfileDocumentDto dto = TestDataUtil.createNewProfileDocumentDto();
        ProfileDocument expected = TestDataUtil.createNewProfileDocument();
        expected.setId(dto.getId());
        // when
        messageListener.createUpdateProfileOnElasticsearch(dto);
        // then
        verify(repository, times(1)).save(expected);
    }

    @Test
    public void testThatDeleteProfileOnElasticSearch() {
        // given
        UUID id = UUID.randomUUID();
        // when
        messageListener.deleteProfileOnElasticSearch(id);
        // then
        verify(repository, times(1)).deleteById(id);
    }
}
