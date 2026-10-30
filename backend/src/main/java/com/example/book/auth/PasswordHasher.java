package com.example.book.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 密码加盐哈希工具
 *
 * 存储格式: sha256$迭代次数$盐(hex)$哈希(hex)
 * 不依赖 Spring Security, 仅使用 JDK 提供的 MessageDigest, 便于课程项目直接运行。
 * 生产环境建议替换为 BCrypt / Argon2。
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "SHA-256";
    private static final int ITERATIONS = 1000;
    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private PasswordHasher() {
    }

    /**
     * 对新密码生成加盐哈希
     */
    public static String encode(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] hash = digest(rawPassword, salt);
        return "sha256$" + ITERATIONS + "$" + HEX.formatHex(salt) + "$" + HEX.formatHex(hash);
    }

    /**
     * 校验明文密码与数据库中存储的哈希是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        String[] parts = encodedPassword.split("\\$");
        if (parts.length != 4 || !"sha256".equals(parts[0])) {
            return false;
        }
        try {
            byte[] salt = HEX.parseHex(parts[2]);
            byte[] expected = HEX.parseHex(parts[3]);
            byte[] actual = digest(rawPassword, salt);
            // 常量时间比较, 避免时序攻击
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] digest(String rawPassword, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] current = rawPassword.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < ITERATIONS; i++) {
                digest.reset();
                digest.update(salt);
                current = digest.digest(current);
            }
            return current;
        } catch (Exception e) {
            throw new IllegalStateException("密码哈希计算失败", e);
        }
    }
}
