package com.permission.service;

import com.permission.common.web.PageResult;
import com.permission.dto.PermissionAssignDTO;
import com.permission.dto.PermissionVO;
import com.permission.dto.RoleEditDTO;
import com.permission.dto.RoleQueryDTO;
import com.permission.dto.RoleSaveDTO;
import com.permission.dto.RoleVO;

import java.util.List;

/**
 * 角色服务：分页查询、增删改、分配权限、权限树。
 */
public interface RoleService {

    /**
     * 分页查询角色。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<RoleVO> queryRolePage(RoleQueryDTO query);

    /**
     * 查询角色详情（含已分配权限）。
     *
     * @param roleId 角色 ID
     * @return 角色详情
     */
    RoleVO queryRoleById(Long roleId);

    /**
     * 查询全量权限树（用于分配权限）。
     *
     * @return 权限树
     */
    List<PermissionVO> queryPermissionTree();

    /**
     * 新增角色。
     *
     * @param saveDTO 新增入参
     */
    void saveRole(RoleSaveDTO saveDTO);

    /**
     * 编辑角色。
     *
     * @param roleId  角色 ID
     * @param editDTO 编辑入参
     */
    void updateRole(Long roleId, RoleEditDTO editDTO);

    /**
     * 删除角色（级联清除角色-权限、用户-角色关联）。
     *
     * @param roleId 角色 ID
     */
    void deleteRole(Long roleId);

    /**
     * 分配角色权限（空集合=清空）。
     *
     * @param roleId     角色 ID
     * @param assignDTO  权限分配入参
     */
    void assignPermissions(Long roleId, PermissionAssignDTO assignDTO);
}
