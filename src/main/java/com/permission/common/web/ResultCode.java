package com.permission.common.web;

import lombok.Getter;

/**
 * 业务码枚举（对照 2.1-项目约束 #7）。
 */
@Getter
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 参数错误 */
    BAD_REQUEST(400, "参数错误"),

    /** 未登录 */
    UNAUTHORIZED(401, "未登录"),

    /** 无权限 */
    FORBIDDEN(403, "无权限"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 系统错误 */
    INTERNAL_ERROR(500, "系统错误"),

    /** 用户名或密码错误 */
    USERNAME_OR_PASSWORD_ERROR(1001, "用户名或密码错误"),

    /** 用户被禁用 */
    USER_DISABLED(1002, "用户被禁用"),

    /** 用户名已存在 */
    USERNAME_EXISTS(1003, "用户名已存在"),

    /** 角色编码已存在 */
    ROLE_CODE_EXISTS(1004, "角色编码已存在"),

    /** 旧密码错误 */
    OLD_PASSWORD_ERROR(1005, "旧密码错误"),

    /** admin 保护操作被拒 */
    ADMIN_PROTECTED(1006, "超级管理员不允许该操作"),

    /** 强制改密未完成 */
    FORCE_CHANGE_REQUIRED(1007, "请先修改初始密码");

    /** 业务码 */
    private final int code;

    /** 默认提示 */
    private final String message;

    /**
     * 构造业务码。
     *
     * @param code    业务码数值
     * @param message 默认提示
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
