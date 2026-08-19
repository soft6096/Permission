package com.permission.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体。
 * 对应表 sys_user，密码为 BCrypt 密文。
 */
@Data
@TableName("sys_user")
public class SysUser {

    /** 主键 ID（雪花） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户名（唯一，存小写） */
    private String username;

    /** 密码（BCrypt 密文） */
    private String password;

    /** 姓名 */
    private String nickname;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    /** 强制改密：0-否 1-是 */
    private Integer forceChange;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删 1-已删 */
    @TableLogic
    private Integer deleted;
}
