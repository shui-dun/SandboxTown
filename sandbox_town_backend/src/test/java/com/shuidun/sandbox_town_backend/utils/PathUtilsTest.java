package com.shuidun.sandbox_town_backend.utils;

import com.shuidun.sandbox_town_backend.service.MapService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PathUtilsTest {

    private final MapService mapService;

    PathUtilsTest(MapService mapService) {
        this.mapService = mapService;
    }

    @Test
    void findPath() {
    }
}