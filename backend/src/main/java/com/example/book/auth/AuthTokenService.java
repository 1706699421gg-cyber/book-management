package com.example.book.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 登录令牌服务: 使用 HmacSHA256 签名的轻量 Token(不依赖第三方 JWT 库)
 *
 * 格式: base64Url(payload).base64Url(hmacSha256(payload))
 * payload 包含 uid / username / role / exp(毫秒时间戳)
 */
@Component
public class AuthTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder decoder = Base64.getUrlDecoder();

    @Value("${app.auth.secret:book-management-default-token-secret}")
    private String secret;

    @Value("${app.auth.expire-minutes:720}")
    private long expireMinutes;

    /**
     * 生成登录令牌
     */
    public String createToken(Long userId, String username, String role) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("uid", userId);
            payload.put("u", username);
            payload.put("r", role);
            payload.put("exp", System.currentTimeMillis() + expireMinutes * 60_000L);
            String body = encoder.encodeToString(objectMapper.writeValueAsBytes(payload));
            return body + "." + encoder.encodeToString(sign(body));
        } catch (Exception e) {
            throw new IllegalStateException("生成登录令牌失败", e);
        }
    }

    /**
     * 令牌有效期(秒), 返回给前端
     */
    public long getExpireSeconds() {
        return expireMinutes * 60L;
    }

    /**
     * 解析并校验令牌; 非法 / 过期 / 被篡改时返回 null
     */
    public TokenPayload parseToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String raw = token.startsWith(BEARER_PREFIX) ? token.substring(BEARER_PREFIX.length()).trim() : token.trim();
        int dot = raw.lastIndexOf('.');
        if (dot <= 0 || dot >= raw.length() - 1) {
            return null;
        }
        String body = raw.substring(0, dot);
        byte[] actual;
        try {
            actual = decoder.decode(raw.substring(dot + 1));
        } catch (IllegalArgumentException e) {
            return null;
        }
        // 常量时间比较签名, 防止伪造
        if (!MessageDigest.isEqual(sign(body), actual)) {
            return null;
        }
        try {
            Map<?, ?> payload = objectMapper.readValue(decoder.decode(body), Map.class);
            Object exp = payload.get("exp");
            if (!(exp instanceof Number) || ((Number) exp).longValue() <= System.currentTimeMillis()) {
                return null;
            }
            Object uid = payload.get("uid");
            Object username = payload.get("u");
            Object role = payload.get("r");
            if (!(uid instanceof Number) || username == null || role == null) {
                return null;
            }
            return new TokenPayload(((Number) uid).longValue(), String.valueOf(username), String.valueOf(role));
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] sign(String body) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("令牌签名失败", e);
        }
    }

    /**
     * 令牌解析结果
     */
    public record TokenPayload(Long userId, String username, String role) {
    }
}
