package com.olelllka.auth_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JWTUtil {

    private String key = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    public String generateAccessJWT(UUID userId) {
        return Jwts.builder()
                .issuer("LinkUp")
                .subject(userId.toString())
                .issuedAt(new Date())
                .signWith(securityKey())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1hr
                .compact();
    }

    public String generateRefreshJWT(UUID userId) {
        return Jwts.builder()
                .issuer("LinkUp")
                .subject(userId.toString())
                .issuedAt(new Date())
                .signWith(securityKey())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .compact();
    }

    public Claims getClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(securityKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public String extractId(String jwt) {
        return getClaims(jwt).getSubject();
    }

    public boolean isTokenValid(UUID userId, String jwt) {
        return extractId(jwt).equals(userId.toString()) && getClaims(jwt).getExpiration().after(Date.from(Instant.now()));
    }

    private SecretKey securityKey() {
        byte[] decodedSecret = Base64.getDecoder().decode(key);
        return Keys.hmacShaKeyFor(decodedSecret);
    }

}
