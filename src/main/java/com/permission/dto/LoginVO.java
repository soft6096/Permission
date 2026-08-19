package com.permission.dto;

import com.permission.common.util.TreeNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录返回体：token + 强制改密标记 + 菜单树。
 */
@Data
public class LoginVO {

    /** 登录凭证 */
    private String token;

    /** 是否强制改密 */
    private boolean forceChange;

    /** 可访问菜单树 */
    private List<MenuVO> menus;
}
