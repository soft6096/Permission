package com.permission.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.permission.common.util.TreeNode;
import lombok.Data;

import java.util.List;

/**
 * 权限树节点（全量权限，用于角色分配权限）。
 */
@Data
public class PermissionVO implements TreeNode<PermissionVO> {

    /** 权限 ID */
    private Long permissionId;

    /** 权限名 */
    private String permissionName;

    /** 权限编码 */
    private String permissionCode;

    /** 父级 ID */
    private Long parentId;

    /** 类型：1-目录 2-页面 */
    private Integer type;

    /** 路由/URL */
    private String route;

    /** 排序 */
    private Integer sort;

    /** 子节点 */
    private List<PermissionVO> children = List.of();

    @Override
    @JsonIgnore
    public Long getId() {
        return permissionId;
    }

    @Override
    public void setChildren(List<PermissionVO> children) {
        this.children = children;
    }
}
