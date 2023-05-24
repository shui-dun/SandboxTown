package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.shuidun.sandbox_town_backend.bean.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/redis")
public class RedisController {
    final RedisTemplate<String, Object> redisTemplate;

    public RedisController(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @RequestMapping("/foo")
    public String foo() {
        log.info("foo triggered");
        Event event = new Event();
        event.setType(Event.EventTypeEnum.FOO);
        event.setData(Map.of("a", "b", "c", "d"));
        redisTemplate.opsForValue().set("test", event);
        return "";
    }

    @RequestMapping("/bar")
    public String bar() {
        Object ans = redisTemplate.opsForValue().get("test");
        log.info("ans: {}", ans);
        return ans.toString();
    }

    @Cacheable(value = "baz", key = "'baz'+#name", unless = "#result == null")
    @RequestMapping("/baz")
    public String baz(String name) {
        log.info("baz triggered");
        return name;
    }

    @Cacheable(value = "users", key = "#id")
    @RequestMapping("/find")
    public Event find(Integer id) {
        log.info("findById triggered");
        Event event = new Event();
        event.setType(Event.EventTypeEnum.FOO);
        event.setData(Map.of("a", "b", "c", "d"));
        return event;
    }
}
