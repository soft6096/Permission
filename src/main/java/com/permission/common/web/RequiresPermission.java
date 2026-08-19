package com.permission.common.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限注解。
 * 标注在 Controller 方法上，由 AuthInterceptor 校验当前登录用户是否拥有对应权限编码。
 * 权限编码格式：模块:资源:操作（如 sys:user:list）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /**
     * 所需权限编码。
     *
     * @return 权限编码
     */
    String value();
}
