package com.permission.controller;

import com.permission.common.util.TokenUtils;
import com.permission.common.web.R;
import com.permission.dto.LoginDTO;
import com.permission.dto.LoginVO;
import com.permission.dto.MenuVO;
import com.permission.dto.PasswordDTO;
import com.permission.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证接口：登录、登出、当前用户菜单树、自助改密。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 认证服务 */
    private final AuthService authService;

    /**
     * 登录：返回 token + 强制改密标记 + 菜单树。
     *
     * @param loginDTO 登录入参
     * @return 登录结果
     */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return R.success(authService.login(loginDTO));
    }

    /**
     * 登出：清除当前凭证。
     *
     * @param request 请求（取 Authorization 头）
     * @return 操作结果
     */
    @PostMapping("/logout")
    public R<Void> logout(HttpServletRequest request) {
        String token = TokenUtils.resolveToken(request);
        if (token != null) {
            authService.logout(token);
        }
        return R.success(null);
    }

    /**
     * 获取当前用户菜单树。
     *
     * @return 菜单树
     */
    @GetMapping("/menus")
    public R<List<MenuVO>> getCurrentUserMenus() {
        return R.success(authService.getCurrentUserMenus());
    }

    /**
     * 自助改密。
     *
     * @param passwordDTO 改密入参
     * @return 操作结果
     */
    @PutMapping("/password")
    public R<Void> changePassword(@Valid @RequestBody PasswordDTO passwordDTO) {
        authService.changePassword(passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
        return R.success(null);
    }
}
