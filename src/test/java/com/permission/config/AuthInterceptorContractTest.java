package com.permission.config;

import com.permission.common.util.RedisTokenStore;
import com.permission.common.util.SecurityUtils;
import com.permission.controller.AuthController;
import com.permission.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 后端接口权限校验契约测试。
 * 来源：3.5-后端接口权限校验-技术方案「验收场景」表。
 * 说明：通过显式 @Import(WebMvcConfig) 注册 AuthInterceptor，验证拦截链路 401/403/1007 判定；
 * 权限不足场景用内嵌探针接口（带 @RequiresPermission）验证（该测试仅加载 AuthController，/sys/** 无 handler）。
 */
@WebMvcTest(controllers = {AuthController.class, TestProbeController.class})
@Import(WebMvcConfig.class)
class AuthInterceptorContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private RedisTokenStore redisTokenStore;

    @MockBean
    private SecurityUtils securityUtils;

    /**
     * 场景 #1：匿名访问白名单 /auth/login 应放行。
     */
    @Test
    void loginShouldPassWhenAnonymous() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk());
    }

    /**
     * 场景 #2：未带 token 访问受保护接口应返回 401。
     */
    @Test
    void menusShouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/auth/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 场景 #3：带无效 token 访问受保护接口应返回 401。
     */
    @Test
    void menusShouldReturn401WhenInvalidToken() throws Exception {
        when(redisTokenStore.getUserId(anyString())).thenReturn(null);

        mockMvc.perform(get("/auth/menus").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }

    /**
     * 场景 #4：已登录但无所需权限编码访问 /sys/** 应返回 403。
     */
    @Test
    void accessSysShouldReturn403WhenNoPermission() throws Exception {
        when(redisTokenStore.getUserId("valid-token")).thenReturn(100L);
        when(securityUtils.getPermissionCodes()).thenReturn(Set.of("sys:user:query"));

        mockMvc.perform(get("/sys/probe-perm").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403));
    }

    /**
     * 场景 #4 补充：已登录且有所需权限编码应放行。
     */
    @Test
    void accessSysShouldPassWhenHasPermission() throws Exception {
        when(redisTokenStore.getUserId("valid-token")).thenReturn(100L);
        when(securityUtils.getPermissionCodes()).thenReturn(Set.of("sys:probe:query"));

        mockMvc.perform(get("/sys/probe-perm").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 场景 #5：强制改密用户访问业务接口应返回 1007。
     */
    @Test
    void menusShouldReturn1007WhenForceChange() throws Exception {
        when(redisTokenStore.getUserId("force-token")).thenReturn(200L);
        when(securityUtils.isForceChange()).thenReturn(true);

        mockMvc.perform(get("/auth/menus").header("Authorization", "Bearer force-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1007));
    }

    /**
     * 场景 #6：强制改密用户访问 /auth/password 应放行。
     */
    @Test
    void changePasswordShouldPassWhenForceChange() throws Exception {
        when(redisTokenStore.getUserId("force-token")).thenReturn(200L);
        when(securityUtils.isForceChange()).thenReturn(true);

        mockMvc.perform(put("/auth/password")
                        .header("Authorization", "Bearer force-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"oldpass123\",\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isOk());
    }

    /**
     * 场景 #7：登出后 token 失效应返回 401。
     */
    @Test
    void menusShouldReturn401WhenTokenRevoked() throws Exception {
        when(redisTokenStore.getUserId("revoked-token")).thenReturn(null);

        mockMvc.perform(get("/auth/menus").header("Authorization", "Bearer revoked-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}
