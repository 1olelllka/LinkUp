package com.olelllka.gateway.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SHA256 {

    private static ThreadLocal<MessageDigest> threadLocal = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-256 Algorithm was not found: " + ex);
        }
    });

    private SHA256() {}

    public static String hash(String input) {
        MessageDigest algo = threadLocal.get();
        byte[] hashed = algo.digest(input.getBytes());
        return Base64.getEncoder().encodeToString(hashed);
    }
}
