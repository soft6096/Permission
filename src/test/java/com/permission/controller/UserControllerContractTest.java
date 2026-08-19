package com.permission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.permission.common.exception.ServiceException;
import com.permission.common.web.PageResult;
import com.permission.common.web.ResultCode;
import com.permission.dto.UserQueryDTO;
import com.permission.dto.UserSaveDTO;
import com.permission.dto.UserVO;
import com.permission.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户管理接口契约测试。
 * 来源：3.2-用户管理-技术方案「验收场景」表（20 个场景，剔除需真实 DB 的级联场景由 Service 单测覆盖）。
 */
@WebMvcTest(UserController.class)
class UserControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    /**
     * 用例：分页查询用户应返回 200 + 分页结构。
     */
    @Test
    void queryUserPageShouldReturn200WithPageResultWhenValid() throws Exception {
        PageResult<UserVO> page = new PageResult<>();
        page.setTotal(1L);
        UserVO vo = new UserVO();
        vo.setUserId(100L);
        vo.setUsername("admin");
        vo.setStatus(1);
        page.setRecords(List.of(vo));
        when(userService.queryUserPage(any(UserQueryDTO.class))).thenReturn(page);

        mockMvc.perform(get("/sys/user/page")
                        .param("username", "adm")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("admin"));
    }

    /**
     * 用例：分页参数 pageSize 超上限应返回 400。
     */
    @Test
    void queryUserPageShouldReturn400WhenPageSizeTooLarge() throws Exception {
        mockMvc.perform(get("/sys/user/page").param("pageSize", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 用例：查询用户详情应返回 200 + 含已分配角色。
     */
    @Test
    void queryUserByIdShouldReturn200WithRoleIdsWhenExist() throws Exception {
        UserVO vo = new UserVO();
        vo.setUserId(100L);
        vo.setUsername("zhangsan");
        vo.setRoleIds(List.of(1L, 2L));
        when(userService.queryUserById(eq(100L))).thenReturn(vo);

        mockMvc.perform(get("/sys/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.roleIds[0]").value(1));
    }

    /**
     * 用例：查询不存在的用户应返回 404。
     */
    @Test
    void queryUserByIdShouldReturn404WhenNotExist() throws Exception {
        when(userService.queryUserById(eq(99999L)))
                .thenThrow(new ServiceException(ResultCode.NOT_FOUND, "用户不存在"));

        mockMvc.perform(get("/sys/user/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    /**
     * 用例：新增用户合法入参应返回 200。
     */
    @Test
    void saveUserShouldReturn200WhenValid() throws Exception {
        UserSaveDTO dto = new UserSaveDTO();
        dto.setUsername("zhangsan");
        dto.setNickname("张三");
        dto.setPassword("abc123456");
        dto.setStatus(1);

        mockMvc.perform(post("/sys/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：新增用户用户名重复应返回 1003。
     */
    @Test
    void saveUserShouldReturn1003WhenUsernameDuplicate() throws Exception {
        org.mockito.Mockito.doThrow(new ServiceException(ResultCode.USERNAME_EXISTS))
                .when(userService).saveUser(any(UserSaveDTO.class));

        UserSaveDTO dto = new UserSaveDTO();
        dto.setUsername("admin");
        dto.setNickname("管理员");
        dto.setPassword("abc123456");
        dto.setStatus(1);

        mockMvc.perform(post("/sys/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1003));
    }

    /**
     * 用例：新增用户密码过短应返回 400。
     */
    @Test
    void saveUserShouldReturn400WhenPasswordTooShort() throws Exception {
        UserSaveDTO dto = new UserSaveDTO();
        dto.setUsername("zhangsan");
        dto.setNickname("张三");
        dto.setPassword("123");
        dto.setStatus(1);

        mockMvc.perform(post("/sys/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 用例：编辑用户合法入参应返回 200。
     */
    @Test
    void updateUserShouldReturn200WhenValid() throws Exception {
        mockMvc.perform(put("/sys/user/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"张三丰\",\"status\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：编辑 admin 用户应返回 1006。
     */
    @Test
    void updateUserShouldReturn1006WhenAdmin() throws Exception {
        org.mockito.Mockito.doThrow(new ServiceException(ResultCode.ADMIN_PROTECTED))
                .when(userService).updateUser(eq(1L), any());

        mockMvc.perform(put("/sys/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"hack\",\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006));
    }

    /**
     * 用例：分配角色应返回 200。
     */
    @Test
    void assignRolesShouldReturn200WhenValid() throws Exception {
        mockMvc.perform(put("/sys/user/100/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：分配角色为空集合应返回 200（清空角色）。
     */
    @Test
    void assignRolesShouldReturn200WhenEmptyRoleIds() throws Exception {
        mockMvc.perform(put("/sys/user/100/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：重置密码应返回 200。
     */
    @Test
    void resetPasswordShouldReturn200WhenValid() throws Exception {
        mockMvc.perform(put("/sys/user/100/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"abc123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：重置密码给 admin 应返回 1006。
     */
    @Test
    void resetPasswordShouldReturn1006WhenAdmin() throws Exception {
        org.mockito.Mockito.doThrow(new ServiceException(ResultCode.ADMIN_PROTECTED))
                .when(userService).resetPassword(eq(1L), any());

        mockMvc.perform(put("/sys/user/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"abc123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006));
    }

    /**
     * 用例：启用/禁用用户应返回 200。
     */
    @Test
    void changeUserStatusShouldReturn200WhenValid() throws Exception {
        mockMvc.perform(put("/sys/user/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：禁用当前登录用户应返回 1006。
     */
    @Test
    void changeUserStatusShouldReturn1006WhenSelfDisabled() throws Exception {
        org.mockito.Mockito.doThrow(new ServiceException(ResultCode.ADMIN_PROTECTED))
                .when(userService).changeUserStatus(any(), any());

        mockMvc.perform(put("/sys/user/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006));
    }

    /**
     * 用例：删除用户应返回 200。
     */
    @Test
    void deleteUserShouldReturn200WhenValid() throws Exception {
        mockMvc.perform(delete("/sys/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：删除 admin 应返回 1006。
     */
    @Test
    void deleteUserShouldReturn1006WhenAdmin() throws Exception {
        org.mockito.Mockito.doThrow(new ServiceException(ResultCode.ADMIN_PROTECTED))
                .when(userService).deleteUser(eq(1L));

        mockMvc.perform(delete("/sys/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006));
    }

    /**
     * 用例：删除不存在的用户应返回 404。
     */
    @Test
    void deleteUserShouldReturn404WhenNotExist() throws Exception {
        org.mockito.Mockito.doThrow(new ServiceException(ResultCode.NOT_FOUND, "用户不存在"))
                .when(userService).deleteUser(eq(99999L));

        mockMvc.perform(delete("/sys/user/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}
