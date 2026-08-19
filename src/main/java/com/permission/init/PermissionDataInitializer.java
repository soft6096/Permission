package com.permission.init;

import com.permission.common.util.AdminGuard;
import com.permission.common.util.PasswordUtil;
import com.permission.domain.SysPermission;
import com.permission.domain.SysRole;
import com.permission.domain.SysRolePermission;
import com.permission.domain.SysUser;
import com.permission.domain.SysUserRole;
import com.permission.mapper.SysPermissionMapper;
import com.permission.mapper.SysRoleMapper;
import com.permission.mapper.SysRolePermissionMapper;
import com.permission.mapper.SysUserMapper;
import com.permission.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限数据初始化：应用启动时幂等预置基础数据。
 * 预置内容：目录"系统管理" + 页面"用户管理/角色管理"、admin 角色（全权限）、admin 账号（强制改密）。
 * 幂等策略：按预置权限编码（sys）是否已存在判断，存在则跳过。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionDataInitializer implements CommandLineRunner {

    /** 权限 Mapper */
    private final SysPermissionMapper sysPermissionMapper;

    /** 角色 Mapper */
    private final SysRoleMapper sysRoleMapper;

    /** 用户 Mapper */
    private final SysUserMapper sysUserMapper;

    /** 用户-角色关联 Mapper */
    private final SysUserRoleMapper sysUserRoleMapper;

    /** 角色-权限关联 Mapper */
    private final SysRolePermissionMapper sysRolePermissionMapper;

    /** 预置权限：系统管理目录 */
    private static final String ROOT_PERMISSION_CODE = "sys";

    /** 预置 admin 初始密码 */
    private static final String ADMIN_INIT_PASSWORD = "admin123";

    /**
     * 启动执行初始化。
     *
     * @param args 启动参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) {
        Long exists = sysPermissionMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getPermissionCode, ROOT_PERMISSION_CODE));
        if (exists > 0) {
            log.info("权限数据已初始化，跳过");
            return;
        }
        // 1. 预置权限：目录 + 页面
        SysPermission root = createPermission("系统管理", ROOT_PERMISSION_CODE, 0L, "", 1, 1);
        SysPermission userPage = createPermission("用户管理", "sys:user", root.getId(), "/sys/user", 2, 1);
        SysPermission rolePage = createPermission("角色管理", "sys:role", root.getId(), "/sys/role", 2, 2);
        // 2. 预置 admin 角色
        SysRole adminRole = new SysRole();
        adminRole.setRoleName("超级管理员");
        adminRole.setRoleCode(AdminGuard.ADMIN_ROLE_CODE);
        adminRole.setDescription("内置超级管理员角色，拥有全部权限");
        sysRoleMapper.insert(adminRole);
        // 3. admin 角色拥有全部权限
        for (SysPermission permission : new SysPermission[]{root, userPage, rolePage}) {
            SysRolePermission rel = new SysRolePermission();
            rel.setRoleId(adminRole.getId());
            rel.setPermissionId(permission.getId());
            sysRolePermissionMapper.insert(rel);
        }
        // 4. 预置 admin 账号（首次登录强制改密）
        SysUser admin = new SysUser();
        admin.setUsername(AdminGuard.ADMIN_USERNAME);
        admin.setPassword(PasswordUtil.encode(ADMIN_INIT_PASSWORD));
        admin.setNickname("超级管理员");
        admin.setStatus(1);
        admin.setForceChange(1);
        sysUserMapper.insert(admin);
        // 5. admin 账号关联 admin 角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(admin.getId());
        userRole.setRoleId(adminRole.getId());
        sysUserRoleMapper.insert(userRole);
        log.info("权限数据初始化完成：权限 {} 条、admin 角色、admin 账号", 3);
    }

    /**
     * 创建权限实体。
     *
     * @param name   权限名
     * @param code   权限编码
     * @param parent 父级 ID
     * @param route  路由/URL
     * @param type   类型（1-目录 2-页面）
     * @param sort   排序
     * @return 权限实体
     */
    private SysPermission createPermission(String name, String code, Long parent, String route, int type, int sort) {
        SysPermission permission = new SysPermission();
        permission.setPermissionName(name);
        permission.setPermissionCode(code);
        permission.setParentId(parent);
        permission.setRoute(route);
        permission.setType(type);
        permission.setSort(sort);
        sysPermissionMapper.insert(permission);
        return permission;
    }
}
