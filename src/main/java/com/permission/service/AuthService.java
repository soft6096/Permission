package com.permission.service;

import com.permission.dto.LoginDTO;
import com.permission.dto.LoginVO;
import com.permission.dto.MenuVO;

import java.util.List;

/**
 * 认证服务：登录、登出、当前用户菜单树、自助改密。
 */
public interface AuthService {

    /**
     * 登录：校验凭证，签发 token，返回菜单树。
     *
     * @param loginDTO 登录入参
     * @return 登录结果（token + 强制改密标记 + 菜单树）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 登出：清除登录凭证。
     *
     * @param token 登录凭证
     */
    void logout(String token);

    /**
     * 获取当前用户可访问菜单树（角色权限并集去重）。
     *
     * @return 菜单树
     */
    List<MenuVO> getCurrentUserMenus();

    /**
     * 自助改密：校验旧密码后修改，成功后清除该用户全部 token 重新登录。
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String oldPassword, String newPassword);
}
