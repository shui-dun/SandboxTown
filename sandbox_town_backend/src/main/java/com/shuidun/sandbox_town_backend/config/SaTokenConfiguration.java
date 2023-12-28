package com.shuidun.sandbox_town_backend.config;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class SaTokenConfiguration implements WebMvcConfigurer {

    @Bean
    @Primary
    public SaTokenConfig getSaTokenConfigPrimary() {
        SaTokenConfig config = new SaTokenConfig();
        config.setTokenName("satoken");             // token名称 (同时也是cookie名称)
        config.setTimeout(7 * 24 * 60 * 60);       // token有效期
        config.setActivityTimeout(-1);              // token临时有效期 (指定时间内无操作就视为token过期) 单位: 秒
        config.setIsConcurrent(false);               // 是否允许同一账号并发登录 (为true时允许一起登录, 为false时新登录挤掉旧登录)
        config.setIsShare(true);                    // 在多人登录同一账号时，是否共用一个token (为true时所有登录共用一个token, 为false时每次登录新建一个token)
        config.setTokenStyle("uuid");               // token风格
        config.setIsLog(false);                     // 是否输出操作日志
        return config;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，定义详细认证规则
        // 参见 https://sa-token.cc/doc.html#/use/route-check
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 指定一条 match 规则
            SaRouter.match("/**") // 拦截的 path 列表，可以写多个
                    .notMatch("/user/login") // 排除掉的 path 列表，可以写多个
                    .notMatch("/user/signup")
                    .notMatch("/user/getUsername")
                    .notMatch("/swagger-ui.html")
                    .notMatch("/swagger-ui/**")
                    .notMatch("/v3/api-docs/**")
                    .check(r -> StpUtil.checkLogin()); // 要执行的校验动作，可以写完整的 lambda 表达式
        })).addPathPatterns("/**");
    }

}