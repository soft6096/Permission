package com.permission.common.base;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 关联表操作抽象基类：提供"先删后插"公共模板方法。
 * 供 UserService.assignRoles、RoleService.assignPermissions 继承复用。
 *
 * @param <R> 关联实体类型
 */
public abstract class AbstractRelService<R> {

    /**
     * 替换关联：先按条件删除旧关联，再插入新关联（空列表=仅清空）。
     * 模板方法，子类传入对应关联 Mapper 与删除条件。
     *
     * @param relMapper     关联 Mapper
     * @param deleteWrapper 删除条件（如 user_id = ?）
     * @param newRows       新关联列表（可为空=清空）
     */
    @Transactional(rollbackFor = Exception.class)
    protected void replaceRelations(BaseMapper<R> relMapper, Wrapper<R> deleteWrapper, List<R> newRows) {
        relMapper.delete(deleteWrapper);
        if (newRows != null && !newRows.isEmpty()) {
            newRows.forEach(relMapper::insert);
        }
    }
}
