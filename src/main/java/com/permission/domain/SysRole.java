package com.permission.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体。
 * 对应表 sys_role。
 */
@Data
@TableName("sys_role")
public class SysRole {

    /** 主键 ID（雪花） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 角色名 */
    private String roleName;

    /** 角色编码（唯一） */
    private String roleCode;

    /** 描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删 1-已删 */
    @TableLogic
    private Integer deleted;
}
