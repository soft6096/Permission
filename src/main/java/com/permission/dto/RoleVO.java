package com.permission.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色返回体。
 */
@Data
public class RoleVO {

    /** 角色 ID */
    private Long roleId;

    /** 角色名 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 描述 */
    private String description;

    /** 已分配权限 ID 列表 */
    private List<Long> permissionIds;
}
