package com.hohoedu.all_pass._core.utils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PaymentKeyGenerator {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate(String centerCode) {
        String dateTime = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        StringBuilder randomPart = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            randomPart.append(CHARSET.charAt(RANDOM.nextInt(CHARSET.length())));
        }
        return centerCode + dateTime + randomPart;
    }
}