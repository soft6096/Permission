package com.permission.service;

import com.permission.common.web.PageResult;
import com.permission.dto.RoleAssignDTO;
import com.permission.dto.UserEditDTO;
import com.permission.dto.UserPasswordResetDTO;
import com.permission.dto.UserQueryDTO;
import com.permission.dto.UserSaveDTO;
import com.permission.dto.UserStatusDTO;
import com.permission.dto.UserVO;

/**
 * 用户服务：分页查询、增删改、分配角色、重置密码、启用/禁用。
 */
public interface UserService {

    /**
     * 分页查询用户。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<UserVO> queryUserPage(UserQueryDTO query);

    /**
     * 查询用户详情（含已分配角色）。
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    UserVO queryUserById(Long userId);

    /**
     * 新增用户。
     *
     * @param saveDTO 新增入参
     */
    void saveUser(UserSaveDTO saveDTO);

    /**
     * 编辑用户。
     *
     * @param userId  用户 ID
     * @param editDTO 编辑入参
     */
    void updateUser(Long userId, UserEditDTO editDTO);

    /**
     * 分配用户角色（空集合=清空）。
     *
     * @param userId     用户 ID
     * @param assignDTO  角色分配入参
     */
    void assignRoles(Long userId, RoleAssignDTO assignDTO);

    /**
     * 重置密码（置强制改密标记并强制下线）。
     *
     * @param userId   用户 ID
     * @param resetDTO 新密码
     */
    void resetPassword(Long userId, UserPasswordResetDTO resetDTO);

    /**
     * 启用/禁用用户。
     *
     * @param userId    用户 ID
     * @param statusDTO 状态
     */
    void changeUserStatus(Long userId, UserStatusDTO statusDTO);

    /**
     * 删除用户（级联清除用户-角色关联）。
     *
     * @param userId 用户 ID
     */
    void deleteUser(Long userId);
}
