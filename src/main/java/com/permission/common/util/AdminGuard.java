package com.permission.common.util;

import com.permission.common.exception.ServiceException;
import com.permission.common.web.ResultCode;

/**
 * 管理员保护工具：admin 账号/角色不可删除、编辑、禁用等。
 */
public final class AdminGuard {

    /** 内置 admin 角色编码 */
    public static final String ADMIN_ROLE_CODE = "admin";

    /** 内置 admin 用户名 */
    public static final String ADMIN_USERNAME = "admin";

    /** 工具类私有构造，禁止实例化 */
    private AdminGuard() {
    }

    /**
     * 校验目标角色不是 admin 角色。
     *
     * @param roleCode 角色编码
     * @throws ServiceException 若为 admin 角色
     */
    public static void assertNotAdminRole(String roleCode) {
        if (ADMIN_ROLE_CODE.equals(roleCode)) {
            throw new ServiceException(ResultCode.ADMIN_PROTECTED);
        }
    }

    /**
     * 校验目标用户不是 admin 账号。
     *
     * @param username 用户名
     * @throws ServiceException 若为 admin 账号
     */
    public static void assertNotAdminUser(String username) {
        if (ADMIN_USERNAME.equals(username)) {
            throw new ServiceException(ResultCode.ADMIN_PROTECTED);
        }
    }

    /**
     * 校验目标用户不是当前登录用户（禁止操作自己）。
     *
     * @param targetUserId    目标用户 ID
     * @param currentUserId   当前登录用户 ID
     * @throws ServiceException 若为同一用户
     */
    public static void assertNotSelf(Long targetUserId, Long currentUserId) {
        if (targetUserId != null && targetUserId.equals(currentUserId)) {
            throw new ServiceException(ResultCode.ADMIN_PROTECTED);
        }
    }
}
