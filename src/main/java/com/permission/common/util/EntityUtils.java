package com.permission.common.util;

import com.permission.common.exception.ServiceException;
import com.permission.common.web.ResultCode;

import java.util.function.Function;

/**
 * 实体操作工具：加载实体或抛业务异常、唯一性校验。
 * 复用于 UserServiceImpl（用户）与 RoleServiceImpl（角色）的加载实体与唯一性校验方法。
 */
public final class EntityUtils {

    /** 工具类私有构造，禁止实例化 */
    private EntityUtils() {
    }

    /**
     * 按 ID 加载实体，不存在抛业务异常。
     *
     * @param id            实体 ID
     * @param finder        查询函数（如 sysUserMapper::selectById）
     * @param resultCode    不存在时的业务码
     * @param notFoundMessage 不存在时的提示
     * @param <T>           实体类型
     * @return 实体
     */
    public static <T> T requireById(Long id, Function<Long, T> finder,
                                    ResultCode resultCode, String notFoundMessage) {
        T entity = finder.apply(id);
        if (entity == null) {
            throw new ServiceException(resultCode, notFoundMessage);
        }
        return entity;
    }

    /**
     * 唯一性校验：重复则抛业务异常。
     *
     * @param count          查询到的记录数
     * @param duplicateCode  重复时的业务码
     */
    public static void checkUnique(long count, ResultCode duplicateCode) {
        if (count > 0) {
            throw new ServiceException(duplicateCode);
        }
    }
}
