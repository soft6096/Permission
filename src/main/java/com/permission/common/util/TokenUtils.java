package com.permission.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * token 解析工具：从请求头 Authorization: Bearer {token} 提取 token。
 * 复用于 AuthInterceptor（权限校验）与 AuthController（登出）。
 */
public final class TokenUtils {

    /** Bearer 前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** 工具类私有构造，禁止实例化 */
    private TokenUtils() {
    }

    /**
     * 从请求头解析 token。
     *
     * @param request HTTP 请求
     * @return token，请求头缺失或格式不符返回 null
     */
    public static String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
