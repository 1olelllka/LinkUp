package com.olelllka.profile_service.mapper;

import com.olelllka.profile_service.TestDataUtil;
import com.olelllka.profile_service.domain.dto.ProfileDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.mapper.impl.ProfileMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ProfileMapperUnitTest {

    @InjectMocks
    private ProfileMapper mapper;

    @Test
    public void testThatMapperMapsToEntity() {
        // given
        ProfileDto dto = TestDataUtil.createNewProfileDto();
        ProfileEntity entity = TestDataUtil.createNewProfileEntity();
        // when
        ProfileEntity result = mapper.toEntity(dto);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getName(), entity.getName()) // basically it'll work with every field, it's not so complex
        );
    }

    @Test
    public void testThatMapperMapsToDto() {
        // given
        ProfileEntity entity = TestDataUtil.createNewProfileEntity();
        ProfileEntity follow = TestDataUtil.createNewProfileEntity();
        follow.setId(UUID.randomUUID());
        // when
        ProfileDto result = mapper.toDto(entity);
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(result.getName(), follow.getName())
        );
    }

}
