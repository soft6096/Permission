package com.permission.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis token 存储工具测试（mock Redis 客户端）。
 */
class RedisTokenStoreTest {

    /**
     * 用例：保存 token 后可通过 token 取回 userId。
     */
    @Test
    @SuppressWarnings("unchecked")
    void saveThenGetUserIdShouldReturnUserId() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        RedisTokenStore store = new RedisTokenStore(redisTemplate, new ObjectMapper());

        store.save(100L, "token-1", new LoginUser());

        // getUserId 从 Redis 读回
        when(valueOps.get("auth:token:token-1")).thenReturn("100");
        assertThat(store.getUserId("token-1")).isEqualTo(100L);
        // save 时写入了 token 与用户集合
        verify(valueOps).set(eq("auth:token:token-1"), eq("100"), any(Duration.class));
        verify(setOps).add("auth:user_tokens:100", "token-1");
    }

    /**
     * 用例：token 不存在时 getUserId 返回 null。
     */
    @Test
    void getUserIdShouldReturnNullWhenTokenNotExist() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:token:ghost")).thenReturn(null);
        RedisTokenStore store = new RedisTokenStore(redisTemplate, new ObjectMapper());

        assertThat(store.getUserId("ghost")).isNull();
    }

    /**
     * 用例：按用户批量清除 token。
     */
    @Test
    @SuppressWarnings("unchecked")
    void removeByUserIdShouldRemoveAllTokens() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        SetOperations<String, String> setOps = mock(SetOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(setOps.members("auth:user_tokens:100")).thenReturn(Set.of("token-1", "token-2"));
        RedisTokenStore store = new RedisTokenStore(redisTemplate, new ObjectMapper());

        store.removeByUserId(100L);

        // 每个 token 的 key 与用户集合都被删除
        verify(redisTemplate).delete("auth:token:token-1");
        verify(redisTemplate).delete("auth:token:token-2");
        verify(redisTemplate).delete("auth:user_tokens:100");
    }
}
