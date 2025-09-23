package com.olelllka.auth_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class JWTUtilUnitTest {

    @InjectMocks
    private JWTUtil jwtUtil;

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
