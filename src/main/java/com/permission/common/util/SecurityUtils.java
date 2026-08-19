package com.permission.common.util;

import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 安全上下文工具：当前登录用户信息存取（ThreadLocal）。
 * 由 AuthInterceptor 在请求进入时写入、请求结束清除。
 */
@Component
public class SecurityUtils {

    /** 当前登录用户上下文（线程隔离） */
    private static final ThreadLocal<LoginUser> LOGIN_USER = new ThreadLocal<>();

    /**
     * 写入当前登录用户。
     *
     * @param loginUser 登录用户
     */
    public void setLoginUser(LoginUser loginUser) {
        LOGIN_USER.set(loginUser);
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户 ID，未登录返回 null
     */
    public Long getUserId() {
        LoginUser loginUser = LOGIN_USER.get();
        return loginUser == null ? null : loginUser.getUserId();
    }

    /**
     * 获取当前登录用户名。
     *
     * @return 用户名，未登录返回 null
     */
    public String getUsername() {
        LoginUser loginUser = LOGIN_USER.get();
        return loginUser == null ? null : loginUser.getUsername();
    }

    /**
     * 当前用户是否强制改密。
     *
     * @return 是否强制改密
     */
    public boolean isForceChange() {
        LoginUser loginUser = LOGIN_USER.get();
        return loginUser != null && loginUser.isForceChange();
    }

    /**
     * 获取当前用户权限编码集合。
     *
     * @return 权限编码集合（未登录返回空集）
     */
    public Set<String> getPermissionCodes() {
        LoginUser loginUser = LOGIN_USER.get();
        return loginUser == null ? Set.of() : loginUser.getPermissionCodes();
    }

    /**
     * 请求结束清除上下文。
     */
    public void clear() {
        LOGIN_USER.remove();
    }
}
