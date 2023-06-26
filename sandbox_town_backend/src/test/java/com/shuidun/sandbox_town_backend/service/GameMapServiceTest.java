package com.shuidun.sandbox_town_backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@SpringBootTest
public class GameMapServiceTest {

    @Autowired
    GameMapService gameMapService;

    // 测试图像中只有黑色或白色
    @Test
    public void testImageVal() throws IOException {
        String imagePath = "static/bitmap/store.png";
        BufferedImage image = ImageIO.read(new ClassPathResource(imagePath).getInputStream());
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] matrix = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 必须是黑色或白色
                assert image.getRGB(x, y) == Color.BLACK.getRGB() || image.getRGB(x, y) == Color.WHITE.getRGB();
            }
        }
    }

    @Test
    void testGenerateMap() {
        gameMapService.generateMap();
        var map = gameMapService.getMap();
        // 打印地图并记录为1的点的数目
        int count = 0;
        for (int[] ints : map) {
            for (int anInt : ints) {
                if (anInt != 0) {
                    count++;
                }
                System.out.printf("%20d", anInt);
            }
            System.out.println();
        }
        // 应该具有至少一个1
        assert count > 0;
    }
}
