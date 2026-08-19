package com.permission.controller;

import com.permission.common.web.PageResult;
import com.permission.common.web.R;
import com.permission.common.web.RequiresPermission;
import com.permission.dto.RoleAssignDTO;
import com.permission.dto.UserEditDTO;
import com.permission.dto.UserPasswordResetDTO;
import com.permission.dto.UserQueryDTO;
import com.permission.dto.UserSaveDTO;
import com.permission.dto.UserStatusDTO;
import com.permission.dto.UserVO;
import com.permission.service.UserService;
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

/**
 * 用户管理接口：分页查询、增删改、分配角色、重置密码、启用/禁用。
 */
@RestController
@RequestMapping("/sys/user")
@Validated
@RequiredArgsConstructor
public class UserController {

    /** 用户服务 */
    private final UserService userService;

    /**
     * 分页查询用户。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @RequiresPermission("sys:user")
    public R<PageResult<UserVO>> queryUserPage(@Valid UserQueryDTO query) {
        return R.success(userService.queryUserPage(query));
    }

    /**
     * 查询用户详情。
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    @GetMapping("/{userId}")
    @RequiresPermission("sys:user")
    public R<UserVO> queryUserById(@PathVariable Long userId) {
        return R.success(userService.queryUserById(userId));
    }

    /**
     * 新增用户。
     *
     * @param saveDTO 新增入参
     * @return 操作结果
     */
    @PostMapping
    @RequiresPermission("sys:user")
    public R<Void> saveUser(@Valid @RequestBody UserSaveDTO saveDTO) {
        userService.saveUser(saveDTO);
        return R.success(null);
    }

    /**
     * 编辑用户。
     *
     * @param userId  用户 ID
     * @param editDTO 编辑入参
     * @return 操作结果
     */
    @PutMapping("/{userId}")
    @RequiresPermission("sys:user")
    public R<Void> updateUser(@PathVariable Long userId, @Valid @RequestBody UserEditDTO editDTO) {
        userService.updateUser(userId, editDTO);
        return R.success(null);
    }

    /**
     * 分配用户角色。
     *
     * @param userId    用户 ID
     * @param assignDTO 角色分配入参
     * @return 操作结果
     */
    @PutMapping("/{userId}/roles")
    @RequiresPermission("sys:user")
    public R<Void> assignRoles(@PathVariable Long userId, @Valid @RequestBody RoleAssignDTO assignDTO) {
        userService.assignRoles(userId, assignDTO);
        return R.success(null);
    }

    /**
     * 重置密码。
     *
     * @param userId   用户 ID
     * @param resetDTO 新密码
     * @return 操作结果
     */
    @PutMapping("/{userId}/password")
    @RequiresPermission("sys:user")
    public R<Void> resetPassword(@PathVariable Long userId, @Valid @RequestBody UserPasswordResetDTO resetDTO) {
        userService.resetPassword(userId, resetDTO);
        return R.success(null);
    }

    /**
     * 启用/禁用用户。
     *
     * @param userId    用户 ID
     * @param statusDTO 状态
     * @return 操作结果
     */
    @PutMapping("/{userId}/status")
    @RequiresPermission("sys:user")
    public R<Void> changeUserStatus(@PathVariable Long userId, @Valid @RequestBody UserStatusDTO statusDTO) {
        userService.changeUserStatus(userId, statusDTO);
        return R.success(null);
    }

    /**
     * 删除用户。
     *
     * @param userId 用户 ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    @RequiresPermission("sys:user")
    public R<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return R.success(null);
    }
}
