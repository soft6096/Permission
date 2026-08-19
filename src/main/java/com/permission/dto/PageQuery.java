package com.permission.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页查询基础入参。
 */
@Data
public class PageQuery {

    /** 页码（从 1 开始） */
    @Min(value = 1, message = "页码从1开始")
    private Integer pageNum = 1;

    /** 每页条数（上限 100） */
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大100")
    private Integer pageSize = 10;
}
