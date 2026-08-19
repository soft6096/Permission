package com.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增用户入参。
 */
@Data
public class UserSaveDTO {

    /** 用户名（大小写不敏感唯一） */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度须在3-50位之间")
    private String username;

    /** 姓名 */
    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过50")
    private String nickname;

    /** 初始密码 */
    @NotBlank(message = "初始密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度须在6-20位之间")
    private String password;

    /** 状态：0-禁用 1-启用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
