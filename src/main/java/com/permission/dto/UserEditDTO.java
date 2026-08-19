package com.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑用户入参。
 */
@Data
public class UserEditDTO {

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String nickname;

    /** 状态：0-禁用 1-启用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
