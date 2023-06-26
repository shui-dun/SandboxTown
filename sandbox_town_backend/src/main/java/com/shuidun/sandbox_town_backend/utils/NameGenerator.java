package com.shuidun.sandbox_town_backend.utils;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class NameGenerator {
    // 生成名字
    public static String generateItemName(String prefix) {
        // 生成UUID
        UUID uuid = UUID.randomUUID();
        // 转换为字符串
        String uuidString = uuidToBase64(uuid);

        // 连接前缀和UUID
        return prefix + '_' + uuidString;
    }

    public static String uuidToBase64(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());

        // 使用 Base64 编码器将字节转换为 Base64 编码的字符串
        return Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array());
    }

}
