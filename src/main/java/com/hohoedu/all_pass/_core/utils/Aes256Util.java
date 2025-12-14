package com.hohoedu.all_pass._core.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class Aes256Util {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int AES_KEY_LENGTH = 32;

    private static SecretKeySpec createKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("AES key cannot be null or empty");
        }

        // ✅ Base64 디코딩
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(key);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("AES key must be Base64 encoded", e);
        }

        // ✅ 디코딩 후 길이 체크
        if (keyBytes.length != AES_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    String.format(
                            "AES key must be exactly %d bytes after Base64 decoding. current=%d bytes",
                            AES_KEY_LENGTH, keyBytes.length
                    )
            );
        }

        return new SecretKeySpec(keyBytes, "AES");
    }


    public static String encrypt(String plainText, String key) {
        // 입력값 검증
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Plain text cannot be null or empty");
        }

        try {
            // 1. 랜덤 IV 생성 (12바이트)
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // 2. Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    createKey(key),
                    new GCMParameterSpec(TAG_LENGTH_BIT, iv)
            );

            // 3. 암호화 수행
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 4. IV + 암호문 결합
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            // 5. Base64 인코딩
            return Base64.getEncoder().encodeToString(buffer.array());

        } catch (IllegalArgumentException e) {
            // 키 관련 에러는 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("AES encryption failed. plainText length: {}", plainText.length(), e);
            throw new IllegalStateException("AES encryption failed", e);
        }
    }

    public static String decrypt(String cipherText, String key) {
        if (cipherText == null || cipherText.isEmpty()) {
            throw new IllegalArgumentException("Cipher text cannot be null or empty");
        }

        try {
            // 1. Base64 디코딩
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            // 2. 최소 길이 검증 (IV 12바이트 + 최소 암호문)
            if (decoded.length < IV_LENGTH_BYTE) {
                throw new IllegalArgumentException(
                        "Invalid cipher text: too short (minimum " + IV_LENGTH_BYTE + " bytes required)"
                );
            }

            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            // 3. IV 추출 (앞 12바이트)
            byte[] iv = new byte[IV_LENGTH_BYTE];
            buffer.get(iv);

            // 4. 암호문 추출 (나머지)
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            // 5. Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    createKey(key),
                    new GCMParameterSpec(TAG_LENGTH_BIT, iv)
            );

            // 6. 복호화 수행
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (IllegalArgumentException e) {
            // 입력값/키 관련 에러는 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("AES decryption failed. Check if the key is correct.", e);
            throw new IllegalStateException("AES decryption failed. Invalid key or corrupted data.", e);
        }
    }
}