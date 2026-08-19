package com.permission.common.util;

import com.permission.dto.MenuVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 菜单树构建工具测试。
 */
class MenuTreeBuilderTest {

    /**
     * 用例：扁平节点应按 parentId 组装为树并排序。
     */
    @Test
    void buildTreeShouldAssembleHierarchy() {
        MenuVO root = menu(1L, 0L, 1);
        MenuVO childA = menu(2L, 1L, 2);
        MenuVO childB = menu(3L, 1L, 1);

        List<MenuVO> tree = MenuTreeBuilder.buildFullTree(List.of(childA, root, childB));

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getId()).isEqualTo(1L);
        // 子节点按 sort 排序：B(1) 在 A(2) 前
        assertThat(tree.get(0).getChildren()).extracting(MenuVO::getId).containsExactly(3L, 2L);
    }

    /**
     * 用例：按授权 ID 过滤应仅返回被授权节点。
     */
    @Test
    void buildTreeShouldFilterByGrantedIds() {
        MenuVO root = menu(1L, 0L, 1);
        MenuVO child = menu(2L, 1L, 1);
        MenuVO other = menu(3L, 0L, 2);

        List<MenuVO> tree = MenuTreeBuilder.buildTree(List.of(root, child, other), java.util.Set.of(1L, 2L));

        assertThat(tree).hasSize(1);
        assertThat(tree.get(0).getId()).isEqualTo(1L);
        assertThat(tree.get(0).getChildren()).extracting(MenuVO::getId).containsExactly(2L);
    }

    /**
     * 用例：授权 ID 为空集合应返回空树。
     */
    @Test
    void buildTreeShouldReturnEmptyWhenNoGranted() {
        MenuVO root = menu(1L, 0L, 1);

        List<MenuVO> tree = MenuTreeBuilder.buildTree(List.of(root), java.util.Set.of());

        assertThat(tree).isEmpty();
    }

    /**
     * 用例：无节点应返回空树。
     */
    @Test
    void buildTreeShouldReturnEmptyWhenNoNodes() {
        List<MenuVO> empty = List.of();
        assertThat(MenuTreeBuilder.buildFullTree(empty)).isEmpty();
        assertThat(MenuTreeBuilder.buildTree(null, null)).isEmpty();
    }

    /**
     * 构造菜单节点。
     *
     * @param id     节点 ID
     * @param parent 父级 ID
     * @param sort   排序
     * @return 菜单节点
     */
    private MenuVO menu(Long id, Long parent, int sort) {
        MenuVO vo = new MenuVO();
        vo.setId(id);
        vo.setParentId(parent);
        vo.setSort(sort);
        return vo;
    }
}
