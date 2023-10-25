package com.shuidun.sandbox_town_backend.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class SecureNameGenerator {

    /** 创建SecureRandom比创建Random对象要慢一些，但是更安全 */
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 生成随机密钥
     */
    public static String generate() {
        // 生成512位（64字节）的随机密钥
        byte[] randomKey = new byte[64];
        secureRandom.nextBytes(randomKey);
        // 转化为Base64字符串
        // getUrlEncoder相比getEncoder，会将Base64字符串中的+和/分别替换为-和_，以便于在URL中使用
        // withoutPadding()方法可以去掉Base64字符串的末尾的=号
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomKey);
    }
}
