package com.olelllka.auth_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JWTUtilUnitTest {

    private static final String SECRET = "0d9aa86975f076cbb84ab112f361a4b254c6f553d41da0918b439300e592ed3f";

    private JWTUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JWTUtil();
        ReflectionTestUtils.setField(jwtUtil, "key", SECRET);
    }

    private SecretKey rawKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
    }

    @Test
    void generateAccessJWT_shouldContainCorrectSubjectAndIssuer() {
        UUID userId = UUID.randomUUID();

        String token = jwtUtil.generateAccessJWT(userId);
        Claims claims = Jwts.parser().verifyWith(rawKey()).build().parseSignedClaims(token).getPayload();

        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("LinkUp", claims.getIssuer());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
    void generateAccessJWT_shouldExpireInApproximatelyOneHour() {
        UUID userId = UUID.randomUUID();
        long before = System.currentTimeMillis();

        String token = jwtUtil.generateAccessJWT(userId);
        Claims claims = Jwts.parser().verifyWith(rawKey()).build().parseSignedClaims(token).getPayload();

        long expectedExpiry = before + 1000 * 60 * 60;
        long actualExpiry = claims.getExpiration().getTime();
        // allow a couple seconds of test execution drift
        assertTrue(Math.abs(actualExpiry - expectedExpiry) < 5000);
    }

    @Test
    void generateRefreshJWT_shouldExpireInApproximatelyOneDay() {
        UUID userId = UUID.randomUUID();
        long before = System.currentTimeMillis();

        String token = jwtUtil.generateRefreshJWT(userId);
        Claims claims = Jwts.parser().verifyWith(rawKey()).build().parseSignedClaims(token).getPayload();

        long expectedExpiry = before + 1000L * 60 * 60 * 24;
        long actualExpiry = claims.getExpiration().getTime();
        assertTrue(Math.abs(actualExpiry - expectedExpiry) < 5000);
    }

    @Test
    void getClaims_shouldReturnParsedClaimsForValidToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateAccessJWT(userId);

        Claims claims = jwtUtil.getClaims(token);

        assertEquals(userId.toString(), claims.getSubject());
    }

    @Test
    void getClaims_shouldThrow_whenTokenSignedWithDifferentKey() {
        UUID userId = UUID.randomUUID();
        SecretKey otherKey = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));
        String tamperedToken = Jwts.builder()
                .issuer("LinkUp")
                .subject(userId.toString())
                .issuedAt(new Date())
                .signWith(otherKey)
                .expiration(new Date(System.currentTimeMillis() + 100000))
                .compact();

        assertThrows(SignatureException.class, () -> jwtUtil.getClaims(tamperedToken));
    }

    @Test
    void getClaims_shouldThrow_whenTokenExpired() {
        UUID userId = UUID.randomUUID();
        String expiredToken = Jwts.builder()
                .issuer("LinkUp")
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .signWith(rawKey())
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.getClaims(expiredToken));
    }

    @Test
    void extractId_shouldReturnSubjectFromToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateAccessJWT(userId);

        assertEquals(userId.toString(), jwtUtil.extractId(token));
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenIdMatchesAndNotExpired() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtil.generateAccessJWT(userId);

        assertTrue(jwtUtil.isTokenValid(userId, token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenUserIdDoesNotMatchSubject() {
        UUID tokenOwner = UUID.randomUUID();
        UUID differentUser = UUID.randomUUID();
        String token = jwtUtil.generateAccessJWT(tokenOwner);

        assertFalse(jwtUtil.isTokenValid(differentUser, token));
    }

    @Test
    void isTokenValid_shouldThrow_whenTokenIsExpired() {
        UUID userId = UUID.randomUUID();
        String expiredToken = Jwts.builder()
                .issuer("LinkUp")
                .subject(userId.toString())
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .signWith(rawKey())
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .compact();

        assertFalse(jwtUtil.isTokenValid(userId, expiredToken));
    }
}