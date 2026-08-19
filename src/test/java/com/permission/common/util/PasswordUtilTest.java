package com.permission.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 密码工具测试：BCrypt 加密与校验。
 */
class PasswordUtilTest {

    /**
     * 用例：正确密码应校验通过。
     */
    @Test
    void matchesShouldReturnTrueWhenPasswordCorrect() {
        String raw = "admin123";
        String encoded = PasswordUtil.encode(raw);

        assertThat(PasswordUtil.matches(raw, encoded)).isTrue();
    }

    /**
     * 用例：错误密码应校验失败。
     */
    @Test
    void matchesShouldReturnFalseWhenPasswordWrong() {
        String encoded = PasswordUtil.encode("admin123");

        assertThat(PasswordUtil.matches("wrongpass", encoded)).isFalse();
    }

    /**
     * 用例：同一明文两次加密应生成不同密文（随机盐）。
     */
    @Test
    void encodeShouldProduceDifferentHashForSamePassword() {
        String encoded1 = PasswordUtil.encode("admin123");
        String encoded2 = PasswordUtil.encode("admin123");

        assertThat(encoded1).isNotEqualTo(encoded2);
        assertThat(PasswordUtil.matches("admin123", encoded1)).isTrue();
        assertThat(PasswordUtil.matches("admin123", encoded2)).isTrue();
    }
}
