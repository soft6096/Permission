package com.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 编辑角色入参。
 */
@Data
public class RoleEditDTO {

    /** 角色名 */
    @NotBlank(message = "角色名不能为空")
    @Size(max = 50, message = "角色名长度不能超过50")
    private String roleName;

    /** 角色编码（不允许修改，仅展示） */
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50")
    private String roleCode;

    /** 描述 */
    @Size(max = 255, message = "描述长度不能超过255")
    private String description;
}
