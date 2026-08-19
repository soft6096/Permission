package com.permission.common.web;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页返回结构。
 *
 * @param <T> 记录类型
 */
@Data
@NoArgsConstructor
public class PageResult<T> {

    /** 总记录数 */
    private long total;

    /** 当前页记录 */
    private List<T> records;

    /**
     * 构造分页结果。
     *
     * @param total   总记录数
     * @param records 当前页记录
     */
    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
