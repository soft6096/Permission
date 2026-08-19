package com.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.common.base.AbstractRelService;
import com.permission.common.exception.ServiceException;
import com.permission.common.util.AdminGuard;
import com.permission.common.util.EntityUtils;
import com.permission.common.util.PasswordUtil;
import com.permission.common.util.RedisTokenStore;
import com.permission.common.util.SecurityUtils;
import com.permission.common.web.PageResult;
import com.permission.common.web.ResultCode;
import com.permission.domain.SysUser;
import com.permission.domain.SysUserRole;
import com.permission.dto.RoleAssignDTO;
import com.permission.dto.UserEditDTO;
import com.permission.dto.UserPasswordResetDTO;
import com.permission.dto.UserQueryDTO;
import com.permission.dto.UserSaveDTO;
import com.permission.dto.UserStatusDTO;
import com.permission.dto.UserVO;
import com.permission.mapper.SysUserMapper;
import com.permission.mapper.SysUserRoleMapper;
import com.permission.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现：分页查询、增删改、分配角色、重置密码、启用/禁用。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends AbstractRelService<SysUserRole> implements UserService {

    /** 用户 Mapper */
    private final SysUserMapper sysUserMapper;

    /** 用户-角色关联 Mapper */
    private final SysUserRoleMapper sysUserRoleMapper;

    /** token 存储 */
    private final RedisTokenStore redisTokenStore;

    /** 安全上下文 */
    private final SecurityUtils securityUtils;

    /**
     * 分页查询用户。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<UserVO> queryUserPage(UserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(query.getUsername() != null && !query.getUsername().isEmpty(), SysUser::getUsername, query.getUsername())
                .like(query.getNickname() != null && !query.getNickname().isEmpty(), SysUser::getNickname, query.getNickname())
                .eq(query.getStatus() != null, SysUser::getStatus, query.getStatus())
                .orderByDesc(SysUser::getCreateTime);
        IPage<SysUser> page = sysUserMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<UserVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records);
    }

    /**
     * 查询用户详情（含已分配角色）。
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    @Override
    public UserVO queryUserById(Long userId) {
        SysUser user = requireUser(userId);
        UserVO vo = toVO(user);
        vo.setRoleIds(queryUserRoleIds(userId));
        return vo;
    }

    /**
     * 新增用户。
     *
     * @param saveDTO 新增入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(UserSaveDTO saveDTO) {
        // 1. 校验用户名唯一（小写统一）
        String username = saveDTO.getUsername().toLowerCase();
        EntityUtils.checkUnique(sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)), ResultCode.USERNAME_EXISTS);
        // 2. 密码 BCrypt 加密后插入
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setNickname(saveDTO.getNickname());
        user.setPassword(PasswordUtil.encode(saveDTO.getPassword()));
        user.setStatus(saveDTO.getStatus());
        user.setForceChange(0);
        sysUserMapper.insert(user);
    }

    /**
     * 编辑用户（admin 保护）。
     *
     * @param userId  用户 ID
     * @param editDTO 编辑入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserEditDTO editDTO) {
        SysUser user = requireUser(userId);
        AdminGuard.assertNotAdminUser(user.getUsername());
        SysUser update = new SysUser();
        update.setId(userId);
        update.setNickname(editDTO.getNickname());
        update.setStatus(editDTO.getStatus());
        sysUserMapper.updateById(update);
    }

    /**
     * 分配用户角色（先删后插，空集合=清空）。
     *
     * @param userId    用户 ID
     * @param assignDTO 角色分配入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, RoleAssignDTO assignDTO) {
        // 1. 校验用户存在 + admin 保护
        SysUser user = requireUser(userId);
        AdminGuard.assertNotAdminUser(user.getUsername());
        // 2. 组装新关联并替换（先删后插，空集合=清空）
        LambdaQueryWrapper<SysUserRole> deleteWrapper =
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId);
        List<SysUserRole> newRows = assignDTO.getRoleIds() == null ? List.of()
                : assignDTO.getRoleIds().stream()
                        .map(roleId -> {
                            SysUserRole rel = new SysUserRole();
                            rel.setUserId(userId);
                            rel.setRoleId(roleId);
                            return rel;
                        })
                        .collect(Collectors.toList());
        replaceRelations(sysUserRoleMapper, deleteWrapper, newRows);
    }

    /**
     * 重置密码（admin 保护，置强制改密并强制下线）。
     *
     * @param userId   用户 ID
     * @param resetDTO 新密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, UserPasswordResetDTO resetDTO) {
        // 1. 校验用户存在 + admin 保护
        SysUser user = requireUser(userId);
        AdminGuard.assertNotAdminUser(user.getUsername());
        // 2. 重置密码并置强制改密标记
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(PasswordUtil.encode(resetDTO.getNewPassword()));
        update.setForceChange(1);
        sysUserMapper.updateById(update);
        // 3. 强制下线：清除该用户全部 token
        redisTokenStore.removeByUserId(userId);
    }

    /**
     * 启用/禁用用户（不可禁用自己）。
     *
     * @param userId    用户 ID
     * @param statusDTO 状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeUserStatus(Long userId, UserStatusDTO statusDTO) {
        // 1. 校验用户存在 + admin 保护 + 禁止禁用自己
        SysUser user = requireUser(userId);
        AdminGuard.assertNotAdminUser(user.getUsername());
        if (statusDTO.getStatus() == 0) {
            AdminGuard.assertNotSelf(userId, securityUtils.getUserId());
        }
        // 2. 更新状态
        SysUser update = new SysUser();
        update.setId(userId);
        update.setStatus(statusDTO.getStatus());
        sysUserMapper.updateById(update);
        // 3. 禁用时强制下线
        if (statusDTO.getStatus() == 0) {
            redisTokenStore.removeByUserId(userId);
        }
    }

    /**
     * 删除用户（不可删除自己，级联清除用户-角色关联）。
     *
     * @param userId 用户 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        // 1. 校验用户存在 + admin 保护 + 禁止删除自己
        SysUser user = requireUser(userId);
        AdminGuard.assertNotAdminUser(user.getUsername());
        AdminGuard.assertNotSelf(userId, securityUtils.getUserId());
        // 2. 级联删除：用户 + 用户-角色关联
        sysUserMapper.deleteById(userId);
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 3. 清除其 token 强制下线
        redisTokenStore.removeByUserId(userId);
    }

    /**
     * 查询用户角色 ID 列表。
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
     * 实体转 VO。
     *
     * @param user 用户实体
     * @return 用户 VO
     */
    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setStatus(user.getStatus());
        vo.setForceChange(user.getForceChange());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    /**
     * 加载用户，不存在抛 404。
     *
     * @param userId 用户 ID
     * @return 用户实体
     */
    private SysUser requireUser(Long userId) {
        return EntityUtils.requireById(userId, sysUserMapper::selectById, ResultCode.NOT_FOUND, "用户不存在");
    }
}
