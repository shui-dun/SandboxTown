package com.shuidun.sandbox_town_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SandboxTownBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SandboxTownBackendApplication.class, args);
    }

}
