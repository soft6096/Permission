package com.permission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.permission.common.web.ResultCode;
import com.permission.dto.LoginDTO;
import com.permission.dto.LoginVO;
import com.permission.dto.MenuVO;
import com.permission.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 认证模块接口契约测试。
 * 来源：3.1-认证模块-技术方案「验收场景」表（14 个场景）。
 */
@WebMvcTest(AuthController.class)
class AuthControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    /**
     * 用例：合法凭证登录应返回 200 + token + 菜单树。
     */
    @Test
    void loginShouldReturn200WithTokenAndMenusWhenValid() throws Exception {
        LoginVO vo = new LoginVO();
        vo.setToken("token-abc");
        vo.setForceChange(true);
        MenuVO menu = new MenuVO();
        menu.setId(1L);
        menu.setPermissionName("系统管理");
        menu.setPermissionCode("sys");
        vo.setMenus(List.of(menu));
        when(authService.login(any(LoginDTO.class))).thenReturn(vo);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginDTO("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("token-abc"))
                .andExpect(jsonPath("$.data.forceChange").value(true))
                .andExpect(jsonPath("$.data.menus[0].permissionCode").value("sys"));
    }

    /**
     * 用例：用户名不存在登录应返回 1001 用户名或密码错误。
     */
    @Test
    void loginShouldReturn1001WhenUserNotExist() throws Exception {
        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new com.permission.common.exception.ServiceException(ResultCode.USERNAME_OR_PASSWORD_ERROR));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    /**
     * 用例：密码错误登录应返回 1001 用户名或密码错误。
     */
    @Test
    void loginShouldReturn1001WhenPasswordWrong() throws Exception {
        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new com.permission.common.exception.ServiceException(ResultCode.USERNAME_OR_PASSWORD_ERROR));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1001));
    }

    /**
     * 用例：禁用用户登录应返回 1002 用户被禁用。
     */
    @Test
    void loginShouldReturn1002WhenUserDisabled() throws Exception {
        when(authService.login(any(LoginDTO.class)))
                .thenThrow(new com.permission.common.exception.ServiceException(ResultCode.USER_DISABLED));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"disabled\",\"password\":\"pass123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1002));
    }

    /**
     * 用例：入参缺 username 应返回 400 参数校验错误。
     */
    @Test
    void loginShouldReturn400WhenUsernameBlank() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"admin123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 用例：登出成功后应返回 200。
     */
    @Test
    void logoutShouldReturn200WhenValidToken() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer token-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：获取当前用户菜单树应返回 200 + 菜单树。
     */
    @Test
    void getCurrentUserMenusShouldReturn200WithMenusWhenValid() throws Exception {
        MenuVO menu = new MenuVO();
        menu.setId(2L);
        menu.setPermissionName("用户管理");
        menu.setPermissionCode("sys:user");
        when(authService.getCurrentUserMenus()).thenReturn(List.of(menu));

        mockMvc.perform(get("/auth/menus")
                        .header("Authorization", "Bearer token-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].permissionCode").value("sys:user"));
    }

    /**
     * 用例：无角色用户菜单应返回 200 + 空数组。
     */
    @Test
    void getCurrentUserMenusShouldReturn200WithEmptyMenusWhenNoRole() throws Exception {
        when(authService.getCurrentUserMenus()).thenReturn(List.of());

        mockMvc.perform(get("/auth/menus")
                        .header("Authorization", "Bearer token-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    /**
     * 用例：自助改密旧密码正确应返回 200。
     */
    @Test
    void changePasswordShouldReturn200WhenOldPasswordCorrect() throws Exception {
        mockMvc.perform(put("/auth/password")
                        .header("Authorization", "Bearer token-abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"oldpass123\",\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：自助改密旧密码错误应返回 1005。
     */
    @Test
    void changePasswordShouldReturn1005WhenOldPasswordWrong() throws Exception {
        org.mockito.Mockito.doThrow(new com.permission.common.exception.ServiceException(ResultCode.OLD_PASSWORD_ERROR))
                .when(authService).changePassword(any(String.class), any(String.class));

        mockMvc.perform(put("/auth/password")
                        .header("Authorization", "Bearer token-abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"wrongpass\",\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1005));
    }

    /**
     * 用例：自助改密新密码过短应返回 400。
     */
    @Test
    void changePasswordShouldReturn400WhenNewPasswordTooShort() throws Exception {
        mockMvc.perform(put("/auth/password")
                        .header("Authorization", "Bearer token-abc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"oldpass123\",\"newPassword\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
