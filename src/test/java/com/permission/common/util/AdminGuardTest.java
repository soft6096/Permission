package com.permission.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.permission.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 管理员保护工具测试。
 */
class AdminGuardTest {

    /**
     * 用例：admin 角色编码应被拒绝。
     */
    @Test
    void assertNotAdminRoleShouldThrowWhenAdminRoleCode() {
        assertThatThrownBy(() -> AdminGuard.assertNotAdminRole("admin"))
                .isInstanceOf(ServiceException.class);
    }

    /**
     * 用例：非 admin 角色编码应放行。
     */
    @Test
    void assertNotAdminRoleShouldPassWhenNormalRoleCode() {
        AdminGuard.assertNotAdminRole("operator");
    }

    /**
     * 用例：admin 用户名应被拒绝。
     */
    @Test
    void assertNotAdminUserShouldThrowWhenAdminUsername() {
        assertThatThrownBy(() -> AdminGuard.assertNotAdminUser("admin"))
                .isInstanceOf(ServiceException.class);
    }

    /**
     * 用例：操作自己应被拒绝。
     */
    @Test
    void assertNotSelfShouldThrowWhenSameUser() {
        assertThatThrownBy(() -> AdminGuard.assertNotSelf(100L, 100L))
                .isInstanceOf(ServiceException.class);
    }

    /**
     * 用例：操作他人应放行。
     */
    @Test
    void assertNotSelfShouldPassWhenDifferentUser() {
        AdminGuard.assertNotSelf(100L, 200L);
    }
}
