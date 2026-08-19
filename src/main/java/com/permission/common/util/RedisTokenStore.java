package com.permission.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/**
 * Redis token 存储工具：登录凭证存取与按用户批量清除。
 * key 约定：
 * auth:token:{token} → userId；
 * auth:user_tokens:{userId} → Set&lt;token&gt;；
 * auth:login_user:{token} → LoginUser（权限编码 + 强制改密标记快照）。
 */
@Component
public class RedisTokenStore {

    /** token 前缀 */
    private static final String TOKEN_KEY_PREFIX = "auth:token:";

    /** 用户 token 集合前缀 */
    private static final String USER_TOKENS_PREFIX = "auth:user_tokens:";

    /** 登录用户快照前缀 */
    private static final String LOGIN_USER_PREFIX = "auth:login_user:";

    /** token 过期时长（小时） */
    private static final long TOKEN_TTL_HOURS = 2;

    /** Redis 客户端 */
    private final StringRedisTemplate redisTemplate;

    /** JSON 序列化器 */
    private final ObjectMapper objectMapper;

    /**
     * 构造 token 存储。
     *
     * @param redisTemplate Redis 客户端
     * @param objectMapper  JSON 序列化器
     */
    public RedisTokenStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 保存登录凭证与用户快照：token → userId、token → LoginUser，并登记用户 token 集合。
     *
     * @param userId    用户 ID
     * @param token     token
     * @param loginUser 登录用户快照（含权限编码/强制改密标记）
     */
    public void save(Long userId, String token, LoginUser loginUser) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, String.valueOf(userId), Duration.ofHours(TOKEN_TTL_HOURS));
        redisTemplate.opsForSet().add(userTokensKey, token);
        redisTemplate.expire(userTokensKey, Duration.ofHours(TOKEN_TTL_HOURS));
        try {
            redisTemplate.opsForValue().set(LOGIN_USER_PREFIX + token,
                    objectMapper.writeValueAsString(loginUser), Duration.ofHours(TOKEN_TTL_HOURS));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("LoginUser 序列化失败", e);
        }
    }

    /**
     * 根据 token 取 userId。
     *
     * @param token token
     * @return 用户 ID，不存在返回 null
     */
    public Long getUserId(String token) {
        String value = redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + token);
        return value == null ? null : Long.valueOf(value);
    }

    /**
     * 根据 token 取登录用户快照。
     *
     * @param token token
     * @return 登录用户快照，不存在返回 null
     */
    public LoginUser getLoginUser(String token) {
        String json = redisTemplate.opsForValue().get(LOGIN_USER_PREFIX + token);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, LoginUser.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("LoginUser 反序列化失败", e);
        }
    }

    /**
     * 清除单个 token。
     *
     * @param token token
     */
    public void remove(String token) {
        Long userId = getUserId(token);
        redisTemplate.delete(TOKEN_KEY_PREFIX + token);
        redisTemplate.delete(LOGIN_USER_PREFIX + token);
        if (userId != null) {
            redisTemplate.opsForSet().remove(USER_TOKENS_PREFIX + userId, token);
        }
    }

    /**
     * 按用户批量清除 token（禁用/删除/重置密码/改密时强制下线）。
     *
     * @param userId 用户 ID
     */
    public void removeByUserId(Long userId) {
        String userTokensKey = USER_TOKENS_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(userTokensKey);
        if (tokens != null) {
            tokens.forEach(this::remove);
        }
        redisTemplate.delete(userTokensKey);
    }
}
