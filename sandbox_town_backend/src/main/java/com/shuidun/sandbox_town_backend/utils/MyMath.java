package com.shuidun.sandbox_town_backend.utils;

import java.util.ArrayList;
import java.util.List;

import com.shuidun.sandbox_town_backend.mixin.GameCache;

public class MyMath {
    public static long safeMod(long a, long b) {
        if (b == 0) throw new IllegalArgumentException("Divisor cannot be zero");
        long result = a % b;
        return result < 0 ? result + b : result;
    }

    /**
     * 泛型轮盘赌选择函数（选择n个，可以重复选择同一元素）
     * @param items 元素列表
     * @param weights 对应的权重列表（必须为正数）
     * @param n 选择的元素个数
     * @return 按权重随机选择的n个元素
     */
    public static <T> List<T> rouletteWheelSelect(List<T> items, List<Double> weights, int n) {
        if (items == null || weights == null || items.size() != weights.size() || items.isEmpty()) {
            throw new IllegalArgumentException("列表不能为空且大小必须相等");
        }
        if (n <= 0) {
            throw new IllegalArgumentException("选择的元素个数必须为正数");
        }

        // 计算总权重
        double totalWeight = 0.0;
        for (double weight : weights) {
            if (weight < 0) {
                throw new IllegalArgumentException("权重必须为非负数");
            }
            totalWeight += weight;
        }

        // 选择n个元素
        List<T> selectedItems = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            // 随机生成一个 [0, totalWeight) 之间的数
            double r = GameCache.random.nextDouble() * totalWeight;

            // 轮盘赌选择
            double cumulativeWeight = 0.0;
            for (int i = 0; i < items.size(); i++) {
                cumulativeWeight += weights.get(i);
                if (r < cumulativeWeight) {
                    selectedItems.add(items.get(i));
                    break;
                }
            }
        }

        return selectedItems;
    }
}
