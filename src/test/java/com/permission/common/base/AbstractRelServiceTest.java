package com.permission.common.base;

import com.permission.domain.SysUserRole;
import com.permission.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 关联表"先删后插"模板方法测试（AbstractRelService 公共组件）。
 */
class AbstractRelServiceTest {

    /**
     * 用例：先删除旧关联，再插入新关联。
     */
    @Test
    void replaceRelationsShouldDeleteThenInsert() {
        SysUserRoleMapper relMapper = mock(SysUserRoleMapper.class);
        TestRelService service = new TestRelService();

        SysUserRole newRel = new SysUserRole();
        newRel.setUserId(100L);
        newRel.setRoleId(1L);
        service.testReplace(relMapper, new ArrayList<>(List.of(newRel)));

        verify(relMapper).delete(any());
        verify(relMapper).insert(org.mockito.ArgumentMatchers.<SysUserRole>any());
    }

    /**
     * 用例：空列表应仅删除不插入。
     */
    @Test
    void replaceRelationsShouldOnlyDeleteWhenEmpty() {
        SysUserRoleMapper relMapper = mock(SysUserRoleMapper.class);
        TestRelService service = new TestRelService();

        service.testReplace(relMapper, new ArrayList<>());

        verify(relMapper).delete(any());
        Mockito.verify(relMapper, Mockito.never()).insert(org.mockito.ArgumentMatchers.<SysUserRole>any());
    }

    /**
     * 测试用子类：暴露 replaceRelations 模板方法。
     */
    static class TestRelService extends AbstractRelService<SysUserRole> {

        /**
         * 调用模板方法。
         *
         * @param mapper 关联 Mapper
         * @param rows   新关联
         */
        void testReplace(SysUserRoleMapper mapper, List<SysUserRole> rows) {
            replaceRelations(mapper,
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, 100L),
                    rows);
        }
    }
}
