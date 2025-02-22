package com.olelllka.auth_service.service;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class JWTUtilUnitTest {

    @InjectMocks
    private JWTUtil jwtUtil;

    @Test
    public void testThatJWTIsBeingGeneratedCorrectlyAndIsValid() {
        // given
        String email = "email@email.com";
        // when
        String jwt = jwtUtil.generateJWT(email);
        // then
        assertEquals(jwtUtil.extractUsername(jwt), email);
        assertTrue(jwtUtil.isTokenValid(email, jwt));
    }

}
