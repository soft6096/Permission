package com.permission.init;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 权限数据初始化测试。
 * 来源：3.4-权限数据初始化-技术方案「验收场景」表。
 */
class PermissionDataInitializerTest {

    /** 权限 Mapper mock */
    private SysPermissionMapper sysPermissionMapper;

    /** 角色 Mapper mock */
    private SysRoleMapper sysRoleMapper;

    /** 用户 Mapper mock */
    private SysUserMapper sysUserMapper;

    /** 用户-角色关联 Mapper mock */
    private SysUserRoleMapper sysUserRoleMapper;

    /** 角色-权限关联 Mapper mock */
    private SysRolePermissionMapper sysRolePermissionMapper;

    /** 初始化器 */
    private PermissionDataInitializer initializer;

    /**
     * 每个用例前构造 mock 与初始化器。
     */
    @BeforeEach
    void setUp() {
        sysPermissionMapper = mock(SysPermissionMapper.class);
        sysRoleMapper = mock(SysRoleMapper.class);
        sysUserMapper = mock(SysUserMapper.class);
        sysUserRoleMapper = mock(SysUserRoleMapper.class);
        sysRolePermissionMapper = mock(SysRolePermissionMapper.class);
        initializer = new PermissionDataInitializer(sysPermissionMapper, sysRoleMapper, sysUserMapper,
                sysUserRoleMapper, sysRolePermissionMapper);
    }

    /**
     * 场景 #1：权限表为空时首次启动应预置全部数据。
     */
    @Test
    void runShouldInitializeDataWhenPermissionEmpty() {
        when(sysPermissionMapper.selectCount(any())).thenReturn(0L);
        // MyBatis-Plus 的 insert 回填主键，mock 需给 id
        when(sysPermissionMapper.insert(any(SysPermission.class)))
                .thenAnswer(invocation -> {
                    SysPermission p = invocation.getArgument(0);
                    p.setId(1L + (p.getSort() - 1));
                    return 1;
                });
        when(sysRoleMapper.insert(any(SysRole.class)))
                .thenAnswer(invocation -> {
                    invocation.<SysRole>getArgument(0).setId(10L);
                    return 1;
                });
        when(sysUserMapper.insert(any(SysUser.class)))
                .thenAnswer(invocation -> {
                    invocation.<SysUser>getArgument(0).setId(20L);
                    return 1;
                });

        initializer.run();

        // admin 角色 + 3 条权限 + 3 条权限关联 + admin 账号 + 1 条用户角色
        verify(sysPermissionMapper, org.mockito.Mockito.times(3)).insert(any(SysPermission.class));
        verify(sysRoleMapper).insert(any(SysRole.class));
        verify(sysRolePermissionMapper, org.mockito.Mockito.times(3)).insert(any(SysRolePermission.class));
        verify(sysUserMapper).insert(any(SysUser.class));
        verify(sysUserRoleMapper).insert(any(SysUserRole.class));
        // admin 账号密码非明文、强制改密
        ArgumentCaptor<SysUser> userCaptor = ArgumentCaptor.forClass(SysUser.class);
        verify(sysUserMapper).insert(userCaptor.capture());
        SysUser admin = userCaptor.getValue();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getPassword()).isNotEqualTo("admin123");
        assertThat(admin.getForceChange()).isEqualTo(1);
        assertThat(admin.getStatus()).isEqualTo(1);
    }

    /**
     * 场景 #2：权限已存在时重复启动应跳过（幂等）。
     */
    @Test
    void runShouldSkipWhenPermissionExists() {
        when(sysPermissionMapper.selectCount(any())).thenReturn(1L);

        initializer.run();

        verify(sysPermissionMapper, never()).insert(any(SysPermission.class));
        verify(sysRoleMapper, never()).insert(any(SysRole.class));
        verify(sysUserMapper, never()).insert(any(SysUser.class));
        verify(sysUserRoleMapper, never()).insert(any(SysUserRole.class));
    }

    /**
     * 场景 #2 补充：admin 角色编码应为 admin。
     */
    @Test
    void runShouldCreateAdminRoleWithCodeAdmin() {
        when(sysPermissionMapper.selectCount(any())).thenReturn(0L);
        when(sysPermissionMapper.insert(any(SysPermission.class)))
                .thenAnswer(invocation -> {
                    SysPermission p = invocation.getArgument(0);
                    p.setId(1L + (p.getSort() - 1));
                    return 1;
                });
        when(sysRoleMapper.insert(any(SysRole.class)))
                .thenAnswer(invocation -> {
                    invocation.<SysRole>getArgument(0).setId(10L);
                    return 1;
                });
        when(sysUserMapper.insert(any(SysUser.class)))
                .thenAnswer(invocation -> {
                    invocation.<SysUser>getArgument(0).setId(20L);
                    return 1;
                });

        initializer.run();

        ArgumentCaptor<SysRole> roleCaptor = ArgumentCaptor.forClass(SysRole.class);
        verify(sysRoleMapper).insert(roleCaptor.capture());
        assertThat(roleCaptor.getValue().getRoleCode()).isEqualTo("admin");
    }
}
