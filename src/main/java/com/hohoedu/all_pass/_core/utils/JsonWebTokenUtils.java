package com.hohoedu.all_pass._core.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import com.hohoedu.all_pass.user.model.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JsonWebTokenUtils {

    private static final String SECRET = "0123456789ABCDEF0123456789ABCDEF";
    private static final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long expiration_ms = 1000L * 60 * 60 * 24;

    public static String create(User user) {
        String jwt = Jwts.builder()
                .subject("project-key")
                .claim("id", user.getUserId())
                .claim("password", user.getPassword())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration_ms))
                .signWith(key)
                .compact();

        System.out.println(jwt);
        return "Bearer " + jwt;
    }

}
