package com.shuidun.sandbox_town_backend.utils;

import com.shuidun.sandbox_town_backend.service.GameMapService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PathUtilsTest {

    private final GameMapService gameMapService;

    PathUtilsTest(GameMapService gameMapService) {
        this.gameMapService = gameMapService;
    }

    @Test
    void findPath() {
    }
}