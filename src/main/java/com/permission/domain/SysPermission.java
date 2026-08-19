package com.permission.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体（菜单/页面）。
 * 对应表 sys_permission，系统预置数据，无维护接口。
 */
@Data
@TableName("sys_permission")
public class SysPermission {

    /** 主键 ID（雪花） */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 权限名 */
    private String permissionName;

    /** 权限编码（唯一） */
    private String permissionCode;

    /** 父级 ID，0=根 */
    private Long parentId;

    /** 路由/URL，目录为空 */
    private String route;

    /** 排序 */
    private Integer sort;

    /** 类型：1-目录 2-页面 */
    private Integer type;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删 1-已删 */
    @TableLogic
    private Integer deleted;
}
