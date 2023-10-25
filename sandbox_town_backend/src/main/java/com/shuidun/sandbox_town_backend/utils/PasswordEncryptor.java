package com.shuidun.sandbox_town_backend.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncryptor {
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** 生成盐和加密后的密码 */
    public static String[] generateSaltedHash(String password) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        return new String[]{saltBase64, encryptedPasswd(password, saltBase64)};
    }

    /** 生成加密后的密码 */
    public static String encryptedPasswd(String password, String salt) {
        try {
            // 使用SHA-256算法进行哈希
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            // 迭代10次
            for (int i = 0; i < 10; i++) {
                // 调用update多次时，这些数据会被拼接（concatenated）在一起，然后执行哈希运算
                md.update(saltBytes);
                md.update(passwordBytes);
                passwordBytes = md.digest();
            }
            return bytesToHex(passwordBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
