package com.permission.dto;

import lombok.Data;

import java.util.List;

/**
 * 分配用户角色入参。
 */
@Data
public class RoleAssignDTO {

    /** 角色 ID 列表（空=清空全部角色） */
    private List<Long> roleIds;
}
