package com.permission.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户列表查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQuery {

    /** 用户名（模糊） */
    private String username;

    /** 姓名（模糊） */
    private String nickname;

    /** 状态：0-禁用 1-启用 */
    private Integer status;
}
