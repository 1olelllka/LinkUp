package com.olelllka.stories_service.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SHA256 {

    private static final ThreadLocal<MessageDigest> threadLocalDigest = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-256 Algorithm was not found: " + ex);
        }
    });

    private SHA256 () {}

    public static String generate(String input)  {
        MessageDigest digest = threadLocalDigest.get();
        byte[] encodedHash = digest.digest(input.getBytes());
        return Base64.getEncoder().encodeToString(encodedHash);
    }
}
