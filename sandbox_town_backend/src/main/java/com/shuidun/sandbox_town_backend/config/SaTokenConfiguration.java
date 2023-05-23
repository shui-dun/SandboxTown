package com.shuidun.sandbox_town_backend.config;

import cn.dev33.satoken.interceptor.SaAnnotationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class SaTokenConfiguration implements WebMvcConfigurer {
    /**
     * 注册注解拦截器 排除不需要注解鉴权的接口地址 (与登录拦截器无关)
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册注解拦截器
        registry.addInterceptor(new SaAnnotationInterceptor())
                // 不需要鉴权的接口地址
                .addPathPatterns("/**");
    }
}