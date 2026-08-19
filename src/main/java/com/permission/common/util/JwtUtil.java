package com.permission.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：签发与解析 token。
 * token 仅作凭证载体（含 userId/username/过期时间），有效性由 RedisTokenStore 判定。
 */
@Component
public class JwtUtil {

    /** 签名密钥 */
    private final SecretKey secretKey;

    /** 过期时长（小时） */
    private final long expireHours;

    /**
     * 构造 JWT 工具。
     *
     * @param secret      签名密钥
     * @param expireHours 过期小时数
     */
    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expire-hours}") long expireHours) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireHours = expireHours;
    }

    /**
     * 签发 token。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return JWT 字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析 token，取出用户 ID。
     *
     * @param token JWT 字符串
     * @return 用户 ID，解析失败返回 null
     */
    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }
}
