package com.hohoedu.all_pass._core.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public class Sha256Util {
    public static String generateSalt() {
        byte[] salt = new byte[16]; // 16~32 바이트 권장
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    public static String sha256(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            md.update(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(md.digest()).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
