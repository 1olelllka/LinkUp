package com.olelllka.profile_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JWTUtil {

    private String key = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

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

}
