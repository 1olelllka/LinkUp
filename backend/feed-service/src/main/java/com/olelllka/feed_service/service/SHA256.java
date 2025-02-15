package com.olelllka.feed_service.service;

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

    private SHA256() {}

    public static String hash(String input) {
        MessageDigest algo = threadLocalDigest.get();
        algo.reset();
        byte[] hash = algo.digest(input.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

}
