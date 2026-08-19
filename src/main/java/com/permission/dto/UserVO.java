package com.permission.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户返回体。
 */
@Data
public class UserVO {

    /** 用户 ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 姓名 */
    private String nickname;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    /** 强制改密：0-否 1-是 */
    private Integer forceChange;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 已分配角色 ID 列表 */
    private List<Long> roleIds;
}
