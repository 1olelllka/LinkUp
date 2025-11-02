package com.olelllka.notification_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JWTUtil {

    @Value("${JWT_SECRET:0d9aa86975f076cbb84ab112f361a4b254c6f553d41da0918b439300e592ed3f}")
    private String key;

    private Claims getClaims(String jwt) {
        return Jwts.parser()
                .verifyWith(securityKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public String extractId(String jwt) {
        return getClaims(jwt).getSubject();
    }

    private SecretKey securityKey() {
        byte[] decodedSecret = Base64.getDecoder().decode(key);
        return Keys.hmacShaKeyFor(decodedSecret);
    }

    public boolean isTokenValid(String jwt) {
        try {
            Claims claims = getClaims(jwt);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false; // invalid signature, malformed, or expired
        }
    }


}
