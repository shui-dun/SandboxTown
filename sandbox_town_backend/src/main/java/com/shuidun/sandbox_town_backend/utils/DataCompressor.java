package com.shuidun.sandbox_town_backend.utils;

import com.shuidun.sandbox_town_backend.bean.Point;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
public class DataCompressor {

    /** 压缩路径 */
    public static List<Integer> compressPath(List<Point> list) {
        // 将 [{"x":3, "y":5}, {"x":3, "y":6}, {"x":3, "y":7}] 压缩成 [3, 5, 3, 6, 3, 7]
        List<Integer> compressedList = new ArrayList<>();
        list.forEach(point -> {
            compressedList.add(point.getX());
            compressedList.add(point.getY());
        });
        return compressedList;
    }

}
