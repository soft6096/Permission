package com.permission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 权限管理模块启动类。
 * 提供登录、用户管理、角色管理、角色分配权限等 RBAC 能力。
 */
@SpringBootApplication
public class PermissionApplication {

    /**
     * 应用入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(PermissionApplication.class, args);
    }
}
