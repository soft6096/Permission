package com.permission.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类：BCrypt 单向加密与校验。
 */
public final class PasswordUtil {

    /** BCrypt 编码器（单例） */
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /** 工具类私有构造，禁止实例化 */
    private PasswordUtil() {
    }

    /**
     * 密码加密。
     *
     * @param rawPassword 明文密码
     * @return BCrypt 密文
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 密码校验。
     *
     * @param rawPassword     明文密码
     * @param encodedPassword BCrypt 密文
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
