package com.permission.dto;

import lombok.Data;

import java.util.List;

/**
 * 分配角色权限入参。
 */
@Data
public class PermissionAssignDTO {

    /** 权限 ID 列表（空=清空全部权限） */
    private List<Long> permissionIds;
}
