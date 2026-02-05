package com.hohoedu.all_pass._core.utils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class KeyGenerator {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();
    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String generateSendKey() {
        // 앞 5자리: 타임스탬프 (Base36)
        long timestamp = System.currentTimeMillis() / 1000; // 초 단위
        String timePart = Long.toString(timestamp, 36).toUpperCase();
        timePart = timePart.substring(timePart.length() - 5); // 뒤 5자리

        // 뒤 3자리: 랜덤
        StringBuilder randomPart = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            randomPart.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return timePart + randomPart.toString();
    }

    public static String generateInviteCode() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(ALPHA_NUMERIC.charAt(random.nextInt(ALPHA_NUMERIC.length())));
        }
        return sb.toString();
    }

    public static String generateManualKey(String centerCode, String yy, String mm) {
        String year = yy.length() >= 2 ? yy.substring(yy.length() - 2) : yy;

        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return centerCode + year + mm + sb.toString();
    }
}
