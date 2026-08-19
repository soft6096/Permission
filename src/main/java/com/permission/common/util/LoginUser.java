package com.permission.common.util;

import lombok.Data;

import java.util.Set;

/**
 * 当前登录用户上下文。
 * 登录成功后存入 Redis（RedisTokenStore），请求时由 AuthInterceptor 载入 SecurityUtils。
 */
@Data
public class LoginUser {

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 是否强制改密 */
    private boolean forceChange;

    /** 权限编码集合（多角色并集去重） */
    private Set<String> permissionCodes;
}
