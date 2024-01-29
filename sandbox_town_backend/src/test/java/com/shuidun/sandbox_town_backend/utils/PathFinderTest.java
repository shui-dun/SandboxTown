package com.shuidun.sandbox_town_backend.utils;

import com.shuidun.sandbox_town_backend.service.GameMapService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PathFinderTest {

    private final GameMapService gameMapService;

    PathFinderTest(GameMapService gameMapService) {
        this.gameMapService = gameMapService;
    }

    @Test
    void findPath() {
    }
}