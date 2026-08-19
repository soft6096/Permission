package com.permission.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色列表查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleQueryDTO extends PageQuery {

    /** 角色名（模糊） */
    private String roleName;

    /** 角色编码（模糊） */
    private String roleCode;
}
