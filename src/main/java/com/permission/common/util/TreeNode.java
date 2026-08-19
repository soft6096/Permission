package com.permission.common.util;

import java.util.List;

/**
 * 菜单树节点接口。
 * 由需参与树构建的 VO 实现（MenuVO / PermissionVO）。
 *
 * @param <T> 节点类型
 */
public interface TreeNode<T> {

    /**
     * 节点 ID。
     *
     * @return 节点 ID
     */
    Long getId();

    /**
     * 父级 ID（0=根）。
     *
     * @return 父级 ID
     */
    Long getParentId();

    /**
     * 排序值。
     *
     * @return 排序值
     */
    Integer getSort();

    /**
     * 设置子节点。
     *
     * @param children 子节点列表
     */
    void setChildren(List<T> children);
}
