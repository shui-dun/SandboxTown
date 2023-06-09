package com.shuidun.sandbox_town_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class SandboxTownBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(SandboxTownBackendApplication.class, args);
    }

}
