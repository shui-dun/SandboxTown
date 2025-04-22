package com.shuidun.sandbox_town_backend.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 最大空闲矩形（MaxRects）装箱实现
 */
public class MaxRects {

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Rect {
        public int x, y, width, height;
        public double centerX() { return x + width / 2.0; }
        public double centerY() { return y + height / 2.0; }
    }

    private final List<Rect> freeRects = new ArrayList<>();
    private final List<Rect> usedRects = new ArrayList<>();

    public MaxRects(int width, int height) {
        freeRects.add(new Rect(0, 0, width, height));
    }

    /**
     * 插入一个矩形（不旋转），返回放置后 Rect；如果无法放下，返回 width=0
     */
    public Rect insert(int width, int height) {
        int bestScore = Integer.MAX_VALUE;
        int bestIndex = -1;
        Rect bestNode = null;

        // 选择“剩余高度最小”空闲区
        for (int i = 0; i < freeRects.size(); i++) {
            Rect free = freeRects.get(i);
            if (free.width >= width && free.height >= height) {
                int score = free.height - height;
                if (score < bestScore) {
                    bestScore = score;
                    bestNode = new Rect(free.x, free.y, width, height);
                    bestIndex = i;
                }
            }
        }

        if (bestNode == null) {
            // 放不下
            return new Rect(0, 0, 0, 0);
        }

        // 拆分空闲区
        splitFreeNode(freeRects.get(bestIndex), bestNode);
        freeRects.remove(bestIndex);
        pruneFreeList();
        usedRects.add(bestNode);

        return bestNode;
    }

    /** 根据 placed 拆分 freeNode */
    private void splitFreeNode(Rect freeNode, Rect placed) {
        // 横切：上下
        if (placed.x < freeNode.x + freeNode.width && placed.x + placed.width > freeNode.x) {
            // 上方空隙
            if (placed.y > freeNode.y) {
                freeRects.add(new Rect(
                    freeNode.x, freeNode.y,
                    freeNode.width, placed.y - freeNode.y
                ));
            }
            // 下方空隙
            if (placed.y + placed.height < freeNode.y + freeNode.height) {
                freeRects.add(new Rect(
                    freeNode.x,
                    placed.y + placed.height,
                    freeNode.width,
                    freeNode.y + freeNode.height - (placed.y + placed.height)
                ));
            }
        }
        // 纵切：左右
        if (placed.y < freeNode.y + freeNode.height && placed.y + placed.height > freeNode.y) {
            // 左侧空隙
            if (placed.x > freeNode.x) {
                freeRects.add(new Rect(
                    freeNode.x, freeNode.y,
                    placed.x - freeNode.x, freeNode.height
                ));
            }
            // 右侧空隙
            if (placed.x + placed.width < freeNode.x + freeNode.width) {
                freeRects.add(new Rect(
                    placed.x + placed.width, freeNode.y,
                    freeNode.x + freeNode.width - (placed.x + placed.width),
                    freeNode.height
                ));
            }
        }
    }

    /** 去掉被包含的空闲区 */
    private void pruneFreeList() {
        for (int i = 0; i < freeRects.size(); i++) {
            Rect a = freeRects.get(i);
            for (int j = i + 1; j < freeRects.size(); j++) {
                Rect b = freeRects.get(j);
                if (isContainedIn(a, b)) {
                    freeRects.remove(i);
                    i--;
                    break;
                }
                if (isContainedIn(b, a)) {
                    freeRects.remove(j);
                    j--;
                }
            }
        }
    }

    private boolean isContainedIn(Rect a, Rect b) {
        return a.x >= b.x &&
               a.y >= b.y &&
               a.x + a.width <= b.x + b.width &&
               a.y + a.height <= b.y + b.height;
    }
}
