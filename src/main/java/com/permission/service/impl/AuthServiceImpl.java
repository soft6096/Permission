package com.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.permission.common.exception.ServiceException;
import com.permission.common.util.AdminGuard;
import com.permission.common.util.JwtUtil;
import com.permission.common.util.LoginUser;
import com.permission.common.util.MenuTreeBuilder;
import com.permission.common.util.PasswordUtil;
import com.permission.common.util.RedisTokenStore;
import com.permission.common.util.SecurityUtils;
import com.permission.common.web.ResultCode;
import com.permission.domain.SysPermission;
import com.permission.domain.SysRole;
import com.permission.domain.SysRolePermission;
import com.permission.domain.SysUser;
import com.permission.domain.SysUserRole;
import com.permission.dto.LoginDTO;
import com.permission.dto.LoginVO;
import com.permission.dto.MenuVO;
import com.permission.mapper.SysPermissionMapper;
import com.permission.mapper.SysRoleMapper;
import com.permission.mapper.SysRolePermissionMapper;
import com.permission.mapper.SysUserMapper;
import com.permission.mapper.SysUserRoleMapper;
import com.permission.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务实现：登录签发 token、登出清凭证、菜单树计算、自助改密。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 用户 Mapper */
    private final SysUserMapper sysUserMapper;

    /** 角色 Mapper */
    private final SysRoleMapper sysRoleMapper;

    /** 用户-角色关联 Mapper */
    private final SysUserRoleMapper sysUserRoleMapper;

    /** 角色-权限关联 Mapper */
    private final SysRolePermissionMapper sysRolePermissionMapper;

    /** 权限 Mapper */
    private final SysPermissionMapper sysPermissionMapper;

    /** JWT 工具 */
    private final JwtUtil jwtUtil;

    /** token 存储 */
    private final RedisTokenStore redisTokenStore;

    /** 安全上下文 */
    private final SecurityUtils securityUtils;

    /**
     * 登录：校验凭证 → 签发 token → 计算菜单树。
     *
     * @param loginDTO 登录入参
     * @return 登录结果
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername().toLowerCase();
        // 1. 查询用户并校验密码（大小写不敏感匹配，失败统一 1001 防用户枚举）
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null || !PasswordUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new ServiceException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        // 2. 校验用户状态：禁用拒绝登录
        if (user.getStatus() == 0) {
            throw new ServiceException(ResultCode.USER_DISABLED);
        }
        // 3. 签发 token 并保存登录用户快照（权限编码 + 强制改密标记）
        String token = jwtUtil.generateToken(user.getId(), username);
        Set<String> permissionCodes = queryPermissionCodes(user.getId());
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(username);
        loginUser.setForceChange(user.getForceChange() == 1);
        loginUser.setPermissionCodes(permissionCodes);
        redisTokenStore.save(user.getId(), token, loginUser);

        // 4. 组装返回体：token + 强制改密标记 + 菜单树
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setForceChange(user.getForceChange() == 1);
        vo.setMenus(queryMenuTree(user.getId()));
        return vo;
    }

    /**
     * 登出：清除当前 token。
     *
     * @param token 登录凭证
     */
    @Override
    public void logout(String token) {
        redisTokenStore.remove(token);
    }

    /**
     * 获取当前用户菜单树。
     *
     * @return 菜单树
     */
    @Override
    public List<MenuVO> getCurrentUserMenus() {
        Long userId = securityUtils.getUserId();
        return queryMenuTree(userId);
    }

    /**
     * 自助改密：校验旧密码 → 更新密码 → 清除全部 token。
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = securityUtils.getUserId();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new ServiceException(ResultCode.OLD_PASSWORD_ERROR);
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(PasswordUtil.encode(newPassword));
        update.setForceChange(0);
        sysUserMapper.updateById(update);
        redisTokenStore.removeByUserId(userId);
    }

    /**
     * 查询用户权限编码集合（多角色并集去重；含 admin 角色则拥有全部权限）。
     *
     * @param userId 用户 ID
     * @return 权限编码集合
     */
    private Set<String> queryPermissionCodes(Long userId) {
        Set<Long> grantedIds = queryGrantedPermissionIds(userId);
        if (grantedIds.isEmpty()) {
            return Set.of();
        }
        return sysPermissionMapper.selectBatchIds(grantedIds).stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toSet());
    }

    /**
     * 查询用户菜单树（角色权限并集去重后组装树）。
     *
     * @param userId 用户 ID
     * @return 菜单树
     */
    private List<MenuVO> queryMenuTree(Long userId) {
        List<SysPermission> allPermissions = sysPermissionMapper.selectList(null);
        Set<Long> grantedIds = queryGrantedPermissionIds(userId);
        if (grantedIds.isEmpty()) {
            return List.of();
        }
        List<MenuVO> menuNodes = allPermissions.stream()
                .map(this::toMenuVO)
                .collect(Collectors.toList());
        return MenuTreeBuilder.buildTree(menuNodes, grantedIds);
    }

    /**
     * 查询用户被授权的权限 ID 集合（多角色并集去重；含 admin 角色则拥有全部权限）。
     *
     * @param userId 用户 ID
     * @return 权限 ID 集合
     */
    private Set<Long> queryGrantedPermissionIds(Long userId) {
        List<Long> roleIds = queryUserRoleIds(userId);
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        boolean hasAdminRole = sysRoleMapper.selectBatchIds(roleIds).stream()
                .anyMatch(role -> AdminGuard.ADMIN_ROLE_CODE.equals(role.getRoleCode()));
        if (hasAdminRole) {
            return sysPermissionMapper.selectList(null).stream()
                    .map(SysPermission::getId)
                    .collect(Collectors.toSet());
        }
        return sysRolePermissionMapper.selectList(
                        new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds))
                .stream().map(SysRolePermission::getPermissionId).collect(Collectors.toSet());
    }

    /**
     * 查询用户全部角色 ID。
     *
     * @param userId 用户 ID
     * @return 角色 ID 列表
     */
    private List<Long> queryUserRoleIds(Long userId) {
        return sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    /**
     * 权限实体转菜单节点。
     *
     * @param permission 权限实体
     * @return 菜单节点
     */
    private MenuVO toMenuVO(SysPermission permission) {
        MenuVO menu = new MenuVO();
        menu.setId(permission.getId());
        menu.setPermissionName(permission.getPermissionName());
        menu.setPermissionCode(permission.getPermissionCode());
        menu.setParentId(permission.getParentId());
        menu.setType(permission.getType());
        menu.setRoute(permission.getRoute());
        menu.setSort(permission.getSort());
        return menu;
    }
}
