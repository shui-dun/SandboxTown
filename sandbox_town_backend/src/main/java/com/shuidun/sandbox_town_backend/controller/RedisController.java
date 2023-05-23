package com.shuidun.sandbox_town_backend.controller;

import com.shuidun.sandbox_town_backend.bean.EventBean;
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
        EventBean eventBean = new EventBean();
        eventBean.setType(EventBean.EventTypeEnum.FOO);
        eventBean.setData(Map.of("a", "b", "c", "d"));
        redisTemplate.opsForValue().set("test", eventBean);
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
    public EventBean find(Integer id) {
        log.info("findById triggered");
        EventBean eventBean = new EventBean();
        eventBean.setType(EventBean.EventTypeEnum.FOO);
        eventBean.setData(Map.of("a", "b", "c", "d"));
        return eventBean;
    }
}
