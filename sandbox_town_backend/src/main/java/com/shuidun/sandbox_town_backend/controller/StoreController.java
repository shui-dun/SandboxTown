package com.shuidun.sandbox_town_backend.controller;

import com.shuidun.sandbox_town_backend.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @RequestMapping("/foo")
    public void foo() {
        storeService.foo();
    }
}

