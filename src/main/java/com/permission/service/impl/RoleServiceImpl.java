package com.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.permission.common.base.AbstractRelService;
import com.permission.common.exception.ServiceException;
import com.permission.common.util.AdminGuard;
import com.permission.common.util.EntityUtils;
import com.permission.common.util.MenuTreeBuilder;
import com.permission.common.web.PageResult;
import com.permission.common.web.ResultCode;
import com.permission.domain.SysPermission;
import com.permission.domain.SysRole;
import com.permission.domain.SysRolePermission;
import com.permission.domain.SysUserRole;
import com.permission.dto.PermissionAssignDTO;
import com.permission.dto.PermissionVO;
import com.permission.dto.RoleEditDTO;
import com.permission.dto.RoleQueryDTO;
import com.permission.dto.RoleSaveDTO;
import com.permission.dto.RoleVO;
import com.permission.mapper.SysPermissionMapper;
import com.permission.mapper.SysRoleMapper;
import com.permission.mapper.SysRolePermissionMapper;
import com.permission.mapper.SysUserRoleMapper;
import com.permission.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现：分页查询、增删改、分配权限、权限树。
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends AbstractRelService<SysRolePermission> implements RoleService {

    /** 角色 Mapper */
    private final SysRoleMapper sysRoleMapper;

    /** 权限 Mapper */
    private final SysPermissionMapper sysPermissionMapper;

    /** 角色-权限关联 Mapper */
    private final SysRolePermissionMapper sysRolePermissionMapper;

    /** 用户-角色关联 Mapper */
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 分页查询角色。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @Override
    public PageResult<RoleVO> queryRolePage(RoleQueryDTO query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .like(query.getRoleName() != null && !query.getRoleName().isEmpty(), SysRole::getRoleName, query.getRoleName())
                .like(query.getRoleCode() != null && !query.getRoleCode().isEmpty(), SysRole::getRoleCode, query.getRoleCode())
                .orderByDesc(SysRole::getCreateTime);
        IPage<SysRole> page = sysRoleMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        List<RoleVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records);
    }

    /**
     * 查询角色详情（含已分配权限）。
     *
     * @param roleId 角色 ID
     * @return 角色详情
     */
    @Override
    public RoleVO queryRoleById(Long roleId) {
        SysRole role = requireRole(roleId);
        RoleVO vo = toVO(role);
        vo.setPermissionIds(sysRolePermissionMapper.selectList(
                        new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId))
                .stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 查询全量权限树。
     *
     * @return 权限树
     */
    @Override
    public List<PermissionVO> queryPermissionTree() {
        List<PermissionVO> nodes = sysPermissionMapper.selectList(null).stream()
                .map(this::toPermissionVO)
                .collect(Collectors.toList());
        return MenuTreeBuilder.buildFullTree(nodes);
    }

    /**
     * 新增角色（角色编码唯一）。
     *
     * @param saveDTO 新增入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRole(RoleSaveDTO saveDTO) {
        EntityUtils.checkUnique(sysRoleMapper.selectCount(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, saveDTO.getRoleCode())), ResultCode.ROLE_CODE_EXISTS);
        SysRole role = new SysRole();
        role.setRoleName(saveDTO.getRoleName());
        role.setRoleCode(saveDTO.getRoleCode());
        role.setDescription(saveDTO.getDescription() == null ? "" : saveDTO.getDescription());
        sysRoleMapper.insert(role);
    }

    /**
     * 编辑角色（admin 保护，roleCode 不允许修改）。
     *
     * @param roleId  角色 ID
     * @param editDTO 编辑入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long roleId, RoleEditDTO editDTO) {
        SysRole role = requireRole(roleId);
        AdminGuard.assertNotAdminRole(role.getRoleCode());
        SysRole update = new SysRole();
        update.setId(roleId);
        update.setRoleName(editDTO.getRoleName());
        update.setDescription(editDTO.getDescription() == null ? "" : editDTO.getDescription());
        sysRoleMapper.updateById(update);
    }

    /**
     * 删除角色（admin 保护，级联清除角色-权限、用户-角色关联）。
     *
     * @param roleId 角色 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        // 1. 校验角色存在 + admin 角色保护
        SysRole role = requireRole(roleId);
        AdminGuard.assertNotAdminRole(role.getRoleCode());
        // 2. 级联删除：角色 + 角色-权限 + 用户-角色关联
        sysRoleMapper.deleteById(roleId);
        sysRolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
    }

    /**
     * 分配角色权限（先删后插，空集合=清空）。
     *
     * @param roleId    角色 ID
     * @param assignDTO 权限分配入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, PermissionAssignDTO assignDTO) {
        // 1. 校验角色存在 + admin 角色保护
        SysRole role = requireRole(roleId);
        AdminGuard.assertNotAdminRole(role.getRoleCode());
        // 2. 组装新关联并替换（先删后插，空集合=清空）
        LambdaQueryWrapper<SysRolePermission> deleteWrapper =
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId);
        List<SysRolePermission> newRows = assignDTO.getPermissionIds() == null ? List.of()
                : assignDTO.getPermissionIds().stream()
                        .map(permissionId -> {
                            SysRolePermission rel = new SysRolePermission();
                            rel.setRoleId(roleId);
                            rel.setPermissionId(permissionId);
                            return rel;
                        })
                        .collect(Collectors.toList());
        replaceRelations(sysRolePermissionMapper, deleteWrapper, newRows);
    }

    /**
     * 加载角色，不存在抛 404。
     *
     * @param roleId 角色 ID
     * @return 角色实体
     */
    private SysRole requireRole(Long roleId) {
        return EntityUtils.requireById(roleId, sysRoleMapper::selectById, ResultCode.NOT_FOUND, "角色不存在");
    }

    /**
     * 角色实体转 VO。
     *
     * @param role 角色实体
     * @return 角色 VO
     */
    private RoleVO toVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setRoleId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        return vo;
    }

    /**
     * 权限实体转权限树节点。
     *
     * @param permission 权限实体
     * @return 权限树节点
     */
    private PermissionVO toPermissionVO(SysPermission permission) {
        PermissionVO vo = new PermissionVO();
        vo.setPermissionId(permission.getId());
        vo.setPermissionName(permission.getPermissionName());
        vo.setPermissionCode(permission.getPermissionCode());
        vo.setParentId(permission.getParentId());
        vo.setType(permission.getType());
        vo.setRoute(permission.getRoute());
        vo.setSort(permission.getSort());
        return vo;
    }
}
