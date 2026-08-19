package com.permission.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT 工具测试。
 */
class JwtUtilTest {

    /** 测试用密钥（≥32 字节，满足 HS256） */
    private static final String SECRET = "test-secret-key-test-secret-key-test-secret-key-0123";

    /**
     * 用例：签发后可解析出正确 userId 与 username。
     */
    @Test
    void parseUserIdShouldReturnUserIdAfterGenerate() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 2);

        String token = jwtUtil.generateToken(100L, "zhangsan");

        assertThat(jwtUtil.parseUserId(token)).isEqualTo(100L);
    }

    /**
     * 用例：非法 token 解析应返回 null。
     */
    @Test
    void parseUserIdShouldReturnNullWhenInvalidToken() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 2);

        assertThat(jwtUtil.parseUserId("invalid-token")).isNull();
    }
}
