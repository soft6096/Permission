package com.permission.controller;

import com.permission.common.web.PageResult;
import com.permission.common.web.R;
import com.permission.common.web.RequiresPermission;
import com.permission.dto.PermissionAssignDTO;
import com.permission.dto.PermissionVO;
import com.permission.dto.RoleEditDTO;
import com.permission.dto.RoleQueryDTO;
import com.permission.dto.RoleSaveDTO;
import com.permission.dto.RoleVO;
import com.permission.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理接口：分页查询、增删改、分配权限、权限树。
 */
@RestController
@RequestMapping("/sys")
@Validated
@RequiredArgsConstructor
public class RoleController {

    /** 角色服务 */
    private final RoleService roleService;

    /**
     * 分页查询角色。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/role/page")
    @RequiresPermission("sys:role")
    public R<PageResult<RoleVO>> queryRolePage(@Valid RoleQueryDTO query) {
        return R.success(roleService.queryRolePage(query));
    }

    /**
     * 查询角色详情。
     *
     * @param roleId 角色 ID
     * @return 角色详情
     */
    @GetMapping("/role/{roleId}")
    @RequiresPermission("sys:role")
    public R<RoleVO> queryRoleById(@PathVariable Long roleId) {
        return R.success(roleService.queryRoleById(roleId));
    }

    /**
     * 查询全量权限树。
     *
     * @return 权限树
     */
    @GetMapping("/permission/tree")
    @RequiresPermission("sys:role")
    public R<List<PermissionVO>> queryPermissionTree() {
        return R.success(roleService.queryPermissionTree());
    }

    /**
     * 新增角色。
     *
     * @param saveDTO 新增入参
     * @return 操作结果
     */
    @PostMapping("/role")
    @RequiresPermission("sys:role")
    public R<Void> saveRole(@Valid @RequestBody RoleSaveDTO saveDTO) {
        roleService.saveRole(saveDTO);
        return R.success(null);
    }

    /**
     * 编辑角色。
     *
     * @param roleId  角色 ID
     * @param editDTO 编辑入参
     * @return 操作结果
     */
    @PutMapping("/role/{roleId}")
    @RequiresPermission("sys:role")
    public R<Void> updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleEditDTO editDTO) {
        roleService.updateRole(roleId, editDTO);
        return R.success(null);
    }

    /**
     * 删除角色。
     *
     * @param roleId 角色 ID
     * @return 操作结果
     */
    @DeleteMapping("/role/{roleId}")
    @RequiresPermission("sys:role")
    public R<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return R.success(null);
    }

    /**
     * 分配角色权限。
     *
     * @param roleId    角色 ID
     * @param assignDTO 权限分配入参
     * @return 操作结果
     */
    @PutMapping("/role/{roleId}/permissions")
    @RequiresPermission("sys:role")
    public R<Void> assignPermissions(@PathVariable Long roleId, @Valid @RequestBody PermissionAssignDTO assignDTO) {
        roleService.assignPermissions(roleId, assignDTO);
        return R.success(null);
    }
}
