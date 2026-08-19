package com.permission.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.permission.common.exception.ServiceException;
import com.permission.common.web.PageResult;
import com.permission.common.web.ResultCode;
import com.permission.dto.PermissionVO;
import com.permission.dto.RoleQueryDTO;
import com.permission.dto.RoleSaveDTO;
import com.permission.dto.RoleVO;
import com.permission.service.RoleService;
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
 * 角色管理接口契约测试。
 * 来源：3.3-角色管理-技术方案「验收场景」表（16 个场景，剔除需真实 DB 的级联场景由 Service 单测覆盖）。
 */
@WebMvcTest(RoleController.class)
class RoleControllerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    /**
     * 用例：分页查询角色应返回 200 + 分页结构。
     */
    @Test
    void queryRolePageShouldReturn200WithPageResultWhenValid() throws Exception {
        PageResult<RoleVO> page = new PageResult<>();
        page.setTotal(1L);
        RoleVO vo = new RoleVO();
        vo.setRoleId(1L);
        vo.setRoleName("超级管理员");
        vo.setRoleCode("admin");
        page.setRecords(List.of(vo));
        when(roleService.queryRolePage(any(RoleQueryDTO.class))).thenReturn(page);

        mockMvc.perform(get("/sys/role/page")
                        .param("roleName", "超"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].roleCode").value("admin"));
    }

    /**
     * 用例：分页参数 pageSize 超上限应返回 400。
     */
    @Test
    void queryRolePageShouldReturn400WhenPageSizeTooLarge() throws Exception {
        mockMvc.perform(get("/sys/role/page").param("pageSize", "1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 用例：查询角色详情应返回 200 + 含已分配权限。
     */
    @Test
    void queryRoleByIdShouldReturn200WithPermissionIdsWhenExist() throws Exception {
        RoleVO vo = new RoleVO();
        vo.setRoleId(1L);
        vo.setRoleName("超级管理员");
        vo.setRoleCode("admin");
        vo.setPermissionIds(List.of(1L, 2L, 3L));
        when(roleService.queryRoleById(eq(1L))).thenReturn(vo);

        mockMvc.perform(get("/sys/role/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.permissionIds[0]").value(1));
    }

    /**
     * 用例：查询不存在的角色应返回 404。
     */
    @Test
    void queryRoleByIdShouldReturn404WhenNotExist() throws Exception {
        when(roleService.queryRoleById(eq(99999L)))
                .thenThrow(new ServiceException(ResultCode.NOT_FOUND, "角色不存在"));

        mockMvc.perform(get("/sys/role/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    /**
     * 用例：查询权限树应返回 200 + 目录/页面层级。
     */
    @Test
    void queryPermissionTreeShouldReturn200WithTreeWhenValid() throws Exception {
        PermissionVO root = new PermissionVO();
        root.setPermissionId(1L);
        root.setPermissionName("系统管理");
        root.setType(1);
        PermissionVO child = new PermissionVO();
        child.setPermissionId(2L);
        child.setPermissionName("用户管理");
        child.setType(2);
        root.setChildren(List.of(child));
        when(roleService.queryPermissionTree()).thenReturn(List.of(root));

        mockMvc.perform(get("/sys/permission/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].children[0].permissionName").value("用户管理"));
    }

    /**
     * 用例：新增角色合法入参应返回 200。
     */
    @Test
    void saveRoleShouldReturn200WhenValid() throws Exception {
        RoleSaveDTO dto = new RoleSaveDTO();
        dto.setRoleName("运营");
        dto.setRoleCode("operator");

        mockMvc.perform(post("/sys/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：新增角色编码重复应返回 1004。
     */
    @Test
    void saveRoleShouldReturn1004WhenCodeDuplicate() throws Exception {
        org.mockito.Mockito.doThrow(new ServiceException(ResultCode.ROLE_CODE_EXISTS))
                .when(roleService).saveRole(any(RoleSaveDTO.class));

        RoleSaveDTO dto = new RoleSaveDTO();
        dto.setRoleName("重复");
        dto.setRoleCode("admin");

        mockMvc.perform(post("/sys/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1004));
    }

    /**
     * 用例：新增角色编码为空应返回 400。
     */
    @Test
    void saveRoleShouldReturn400WhenRoleCodeBlank() throws Exception {
        mockMvc.perform(post("/sys/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"运营\",\"roleCode\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 用例：编辑角色应返回 200。
     */
    @Test
    void updateRoleShouldReturn200WhenValid() throws Exception {
        mockMvc.perform(put("/sys/role/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"运营专员\",\"roleCode\":\"operator\",\"description\":\"运营角色\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：编辑 admin 角色应返回 1006。
     */
    @Test
    void updateRoleShouldReturn1006WhenAdminRole() throws Exception {
        org.mockito.Mockito.doThrow(new ServiceException(ResultCode.ADMIN_PROTECTED))
                .when(roleService).updateRole(eq(1L), any());

        mockMvc.perform(put("/sys/role/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleName\":\"hack\",\"roleCode\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006));
    }

    /**
     * 用例：删除角色应返回 200。
     */
    @Test
    void deleteRoleShouldReturn200WhenValid() throws Exception {
        mockMvc.perform(delete("/sys/role/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：删除 admin 角色应返回 1006。
     */
    @Test
    void deleteRoleShouldReturn1006WhenAdminRole() throws Exception {
        org.mockito.Mockito.doThrow(new ServiceException(ResultCode.ADMIN_PROTECTED))
                .when(roleService).deleteRole(eq(1L));

        mockMvc.perform(delete("/sys/role/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006));
    }

    /**
     * 用例：分配角色权限应返回 200。
     */
    @Test
    void assignPermissionsShouldReturn200WhenValid() throws Exception {
        mockMvc.perform(put("/sys/role/2/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissionIds\":[1,2,3]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：分配权限为空集合应返回 200（清空权限）。
     */
    @Test
    void assignPermissionsShouldReturn200WhenEmptyPermissionIds() throws Exception {
        mockMvc.perform(put("/sys/role/2/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissionIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 用例：分配权限给 admin 角色应返回 1006。
     */
    @Test
    void assignPermissionsShouldReturn1006WhenAdminRole() throws Exception {
        org.mockito.Mockito.doThrow(new ServiceException(ResultCode.ADMIN_PROTECTED))
                .when(roleService).assignPermissions(eq(1L), any());

        mockMvc.perform(put("/sys/role/1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissionIds\":[1]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1006));
    }
}
