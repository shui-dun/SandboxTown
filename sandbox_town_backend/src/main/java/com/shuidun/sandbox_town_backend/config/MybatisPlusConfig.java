package com.shuidun.sandbox_town_backend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.shuidun.sandbox_town_backend.mapper")
public class MybatisPlusConfig {
}