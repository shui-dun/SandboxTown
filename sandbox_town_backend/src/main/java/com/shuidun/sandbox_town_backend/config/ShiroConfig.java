package com.shuidun.sandbox_town_backend.config;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class ShiroConfig {
    @Autowired
    private CustomRealm customRealm;

    /**
     * 配置shiro的过滤器工厂（哪些路径需要哪些权限才能访问）
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean filterFactoryBean = new ShiroFilterFactoryBean();
        // 关联SecurityManager
        filterFactoryBean.setSecurityManager(securityManager);
        // 添加shiro过滤器
        Map<String, String> map = new LinkedHashMap<>();
        map.put("/**", "anon");
        filterFactoryBean.setFilterChainDefinitionMap(map);
        return filterFactoryBean;
    }

    /**
     * 配置SecurityManager，与安全有关的操作都会与SecurityManager进行交互；管理着所有的Subject；它是Shiro的核心，负责与Shiro的其他组件进行交互
     */
    @Bean
    public DefaultWebSecurityManager getDefaultWebSecurityManager() {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        //设置登录登录认证开启salt加密
        HashedCredentialsMatcher hashMatcher = new HashedCredentialsMatcher();
        hashMatcher.setHashAlgorithmName(Sha256Hash.ALGORITHM_NAME);
        hashMatcher.setStoredCredentialsHexEncoded(false);
        hashMatcher.setHashIterations(3);
        customRealm.setCredentialsMatcher(hashMatcher);
        manager.setRealm(customRealm);
        return manager;
    }

}
