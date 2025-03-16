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
        String jwt = jwtUtil.generateJWT(id);
        // then
        assertEquals(jwtUtil.extractId(jwt), id.toString());
        assertTrue(jwtUtil.isTokenValid(id, jwt));
    }

}
