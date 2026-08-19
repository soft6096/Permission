package com.permission.dto;

import com.permission.common.util.TreeNode;
import lombok.Data;

import java.util.List;

/**
 * 菜单树节点。
 */
@Data
public class MenuVO implements TreeNode<MenuVO> {

    /** 节点 ID */
    private Long id;

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
    private List<MenuVO> children = List.of();

    @Override
    public void setChildren(List<MenuVO> children) {
        this.children = children;
    }
}
