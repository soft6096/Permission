package com.permission.common.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单树构建工具：将扁平权限列表按 parentId 组装为树。
 * 复用于登录菜单树、/auth/menus、/sys/permission/tree 三处。
 */
public final class MenuTreeBuilder {

    /** 工具类私有构造，禁止实例化 */
    private MenuTreeBuilder() {
    }

    /**
     * 构建全量树（不裁剪，用于权限分配树）。
     *
     * @param allNodes 全部节点
     * @param <T>      节点类型
     * @return 根节点树
     */
    public static <T extends TreeNode<T>> List<T> buildFullTree(List<T> allNodes) {
        return buildTree(allNodes, null);
    }

    /**
     * 构建有权限节点树（按 grantedIds 过滤，用于登录/菜单）。
     *
     * @param allNodes   全部节点
     * @param grantedIds 被授权的节点 ID 集合（null 表示不过滤）
     * @param <T>        节点类型
     * @return 根节点树
     */
    public static <T extends TreeNode<T>> List<T> buildTree(List<T> allNodes, Set<Long> grantedIds) {
        if (allNodes == null || allNodes.isEmpty()) {
            return List.of();
        }
        // 1. 过滤出有权限节点（grantedIds 为 null 时保留全部）
        List<T> filtered = grantedIds == null
                ? new ArrayList<>(allNodes)
                : allNodes.stream()
                        .filter(node -> grantedIds.contains(node.getId()))
                        .toList();
        if (filtered.isEmpty()) {
            return List.of();
        }
        // 2. 分组：parentId → children
        Map<Long, List<T>> childrenMap = filtered.stream()
                .collect(Collectors.groupingBy(TreeNode::getParentId));
        // 3. 找根节点（parentId 不在集合内或为 0 视为根），递归挂载子节点，按 sort 排序
        Set<Long> nodeIds = filtered.stream().map(TreeNode::getId).collect(Collectors.toSet());
        List<T> roots = filtered.stream()
                .filter(node -> node.getParentId() == null
                        || node.getParentId() == 0L
                        || !nodeIds.contains(node.getParentId()))
                .sorted(Comparator.comparingInt(node -> node.getSort() == null ? 0 : node.getSort()))
                .toList();
        roots.forEach(root -> attachChildren(root, childrenMap));
        return roots;
    }

    /**
     * 递归挂载子节点。
     *
     * @param node        当前节点
     * @param childrenMap parentId → 子节点列表
     * @param <T>         节点类型
     */
    private static <T extends TreeNode<T>> void attachChildren(T node, Map<Long, List<T>> childrenMap) {
        List<T> children = childrenMap.getOrDefault(node.getId(), List.of()).stream()
                .sorted(Comparator.comparingInt(n -> n.getSort() == null ? 0 : n.getSort()))
                .toList();
        children.forEach(child -> attachChildren(child, childrenMap));
        node.setChildren(children);
    }
}
