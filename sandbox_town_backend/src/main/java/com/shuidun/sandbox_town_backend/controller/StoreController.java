package com.shuidun.sandbox_town_backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/store")
public class StoreController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/foo")
    public String foo() {
        logger.info("foo triggered");
        return "foo";
    }
}
