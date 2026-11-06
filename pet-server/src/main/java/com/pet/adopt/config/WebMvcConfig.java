package com.pet.adopt.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Web 配置：注册登录拦截器 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /**
     * 注册登录拦截器：公开接口与角色校验全部在 AuthInterceptor 内部按路径完成，
     * 这里不再单独排除路径，保证「未登录 / 角色不符」能返回统一的业务提示。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注意：server.servlet.context-path=/api 会被 Spring 从请求路径中剥离，
        // 所以这里写 "/**"（对外即 /api/**）。
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**");
    }
}
