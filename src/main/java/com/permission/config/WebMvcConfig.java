package com.permission.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.permission.common.util.RedisTokenStore;
import com.permission.common.util.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：注册认证与权限拦截器。
 * 说明：不实现 WebMvcConfigurer 接口而用 @Bean 暴露，避免 @WebMvcTest 切片测试自动加载拦截器
 * （仅 AuthInterceptorContractTest 显式 @Import 本类）。
 */
@Configuration
public class WebMvcConfig {

    /**
     * 认证拦截器 Bean。
     *
     * @param redisTokenStore token 存储
     * @param securityUtils   安全上下文
     * @param objectMapper    JSON 序列化器
     * @return 认证拦截器
     */
    @Bean
    public AuthInterceptor authInterceptor(RedisTokenStore redisTokenStore, SecurityUtils securityUtils, ObjectMapper objectMapper) {
        return new AuthInterceptor(redisTokenStore, securityUtils, objectMapper);
    }

    /**
     * 注册拦截器：除 /auth/login 外全部路径进入认证与权限校验。
     *
     * @param authInterceptor 认证拦截器
     * @return MVC 配置
     */
    @Bean
    public WebMvcConfigurer authWebMvcConfigurer(AuthInterceptor authInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(authInterceptor)
                        .addPathPatterns("/**")
                        .excludePathPatterns("/auth/login");
            }
        };
    }
}
