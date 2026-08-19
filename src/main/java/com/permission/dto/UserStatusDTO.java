package com.permission.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 启用/禁用用户入参。
 */
@Data
public class UserStatusDTO {

    /** 状态：0-禁用 1-启用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
