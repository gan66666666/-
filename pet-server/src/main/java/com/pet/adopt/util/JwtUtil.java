package com.pet.adopt.util;

import com.pet.adopt.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：生成 / 解析 / 读取令牌
 */
@Slf4j
@Component
public class JwtUtil {

    /** 用户ID 在令牌中的 claim 名称 */
    public static final String CLAIM_USER_ID = "userId";

    /** 角色 在令牌中的 claim 名称 */
    public static final String CLAIM_ROLE = "role";

    /** 管理员角色 */
    public static final String ROLE_ADMIN = "admin";

    /** 普通用户角色 */
    public static final String ROLE_USER = "user";

    /** 令牌过期提示语 */
    private static final String INVALID_TOKEN_MSG = "未登录或登录已过期";

    /** 签名密钥，来自配置项 pet.jwt.secret，禁止硬编码 */
    @Value("${pet.jwt.secret}")
    private String secret;

    /** 令牌有效期（小时），来自配置项 pet.jwt.expire-hours */
    @Value("${pet.jwt.expire-hours:24}")
    private Long expireHours;

    /**
     * 生成令牌：载荷放入用户 id、用户名与角色，默认 24 小时过期
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色：admin（管理端）或 user（用户端）
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireHours * 60 * 60 * 1000);
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析令牌，失败抛出业务异常
     */
    public Claims parseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(INVALID_TOKEN_MSG);
        }
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("令牌解析失败: {}", e.getMessage());
            throw new BusinessException(INVALID_TOKEN_MSG);
        }
    }

    /**
     * 从令牌中取出用户ID
     */
    public Long getUserId(String token) {
        Object userId = parseToken(token).get(CLAIM_USER_ID);
        if (userId == null) {
            throw new BusinessException(INVALID_TOKEN_MSG);
        }
        return Long.valueOf(userId.toString());
    }

    /**
     * 从令牌中取出用户名
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从令牌中取出角色，令牌没有 role 声明时返回 null
     */
    public String getRole(String token) {
        Object role = parseToken(token).get(CLAIM_ROLE);
        return role == null ? null : role.toString();
    }

    /**
     * 构造 HMAC 签名密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
