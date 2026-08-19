package com.permission.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.permission.common.util.LoginUser;
import com.permission.common.util.RedisTokenStore;
import com.permission.common.util.SecurityUtils;
import com.permission.common.util.TokenUtils;
import com.permission.common.web.R;
import com.permission.common.web.RequiresPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 认证与权限拦截器。
 * 校验登录凭证（401）、强制改密（1007）、接口权限编码（403）。
 * 白名单：POST /auth/login 匿名；/auth/logout、/auth/password、/auth/menus 仅需登录。
 */
public class AuthInterceptor implements HandlerInterceptor {

    /** 白名单路径（匿名可访问） */
    private static final String ANONYMOUS_PATH = "/auth/login";

    /** 仅需登录路径（无需权限编码，且强制改密放行） */
    private static final String LOGIN_ONLY_PREFIX = "/auth/";

    /** token 存储 */
    private final RedisTokenStore redisTokenStore;

    /** 安全上下文 */
    private final SecurityUtils securityUtils;

    /** JSON 序列化器 */
    private final ObjectMapper objectMapper;

    /**
     * 构造拦截器。
     *
     * @param redisTokenStore token 存储
     * @param securityUtils   安全上下文
     * @param objectMapper    JSON 序列化器
     */
    public AuthInterceptor(RedisTokenStore redisTokenStore, SecurityUtils securityUtils, ObjectMapper objectMapper) {
        this.redisTokenStore = redisTokenStore;
        this.securityUtils = securityUtils;
        this.objectMapper = objectMapper;
    }

    /**
     * 请求前置校验：认证 + 强制改密 + 权限编码。
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return 是否放行
     * @throws IOException IO 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();
        // 1. 白名单路径直接放行
        if (ANONYMOUS_PATH.equals(path)) {
            return true;
        }
        // 2. 校验登录凭证：token 缺失或无效 → 401
        String token = TokenUtils.resolveToken(request);
        if (token == null) {
            return reject(response, 401, "未登录");
        }
        Long userId = redisTokenStore.getUserId(token);
        if (userId == null) {
            return reject(response, 401, "未登录");
        }
        // 3. 载入登录用户上下文（权限编码 + 强制改密标记）
        LoginUser loginUser = redisTokenStore.getLoginUser(token);
        if (loginUser != null) {
            securityUtils.setLoginUser(loginUser);
        }
        boolean isAuthPath = path.startsWith(LOGIN_ONLY_PREFIX);
        boolean forceChangeExempt = "/auth/password".equals(path) || "/auth/logout".equals(path);
        // 4. 强制改密拦截：除改密/登出外，强制改密用户一律拦截
        if (!forceChangeExempt && securityUtils.isForceChange()) {
            return reject(response, 1007, "请先修改初始密码");
        }
        // 5. 业务接口校验权限编码：已登录但无所需权限 → 403
        if (!isAuthPath && handler instanceof HandlerMethod handlerMethod) {
            RequiresPermission requiresPermission = handlerMethod.getMethodAnnotation(RequiresPermission.class);
            if (requiresPermission != null && !securityUtils.getPermissionCodes().contains(requiresPermission.value())) {
                return reject(response, 403, "无权限");
            }
        }
        return true;
    }

    /**
     * 请求结束清除用户上下文。
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @param ex       异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        securityUtils.clear();
    }

    /**
     * 写出拒绝响应。
     *
     * @param response 响应
     * @param code     业务码
     * @param message  提示
     * @return false（拦截）
     * @throws IOException IO 异常
     */
    private boolean reject(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(R.error(code, message)));
        return false;
    }
}
