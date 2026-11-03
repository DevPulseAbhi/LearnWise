package com.learnwise.security;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public final class Crypto {
    private Crypto() {}
    public static String randomToken() {
        var bytes = new byte[32]; new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    public static byte[] sha256(String value) {
        try { return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 is unavailable", exception); }
    }
    public static boolean constantTimeEquals(byte[] left, byte[] right) { return MessageDigest.isEqual(left, right); }
}
