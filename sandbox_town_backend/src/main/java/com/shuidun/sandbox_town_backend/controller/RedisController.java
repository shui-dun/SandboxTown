package com.shuidun.sandbox_town_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/redis")
public class RedisController {
    final RedisTemplate<String, String> redisTemplate;

    public RedisController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @RequestMapping("/foo")
    public String foo() {
        redisTemplate.opsForValue().set("test", "xixi");
        return "";
    }

    @RequestMapping("/bar")
    public String bar() {
        String ans = redisTemplate.opsForValue().get("test");
        log.info("ans: {}", ans);
        return ans;
    }
}
