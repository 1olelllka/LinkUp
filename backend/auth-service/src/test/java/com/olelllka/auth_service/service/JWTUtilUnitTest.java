package com.olelllka.auth_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class JWTUtilUnitTest {

    @InjectMocks
    private JWTUtil jwtUtil;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "key", "0d9aa86975f076cbb84ab112f361a4b254c6f553d41da0918b439300e592ed3f");
    }

    @Test
    public void testThatJWTIsBeingGeneratedCorrectlyAndIsValid() {
        // given
        UUID id = UUID.randomUUID();
        // when
        String accessJWT = jwtUtil.generateAccessJWT(id);
        String refreshJWT = jwtUtil.generateRefreshJWT(id);
        // then
        assertEquals(jwtUtil.extractId(accessJWT), id.toString());
        assertTrue(jwtUtil.isTokenValid(id, accessJWT));
        assertEquals(jwtUtil.extractId(refreshJWT), id.toString());
        assertTrue(jwtUtil.isTokenValid(id, refreshJWT));
    }

}
