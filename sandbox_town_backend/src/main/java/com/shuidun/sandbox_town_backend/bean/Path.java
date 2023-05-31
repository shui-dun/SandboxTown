package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Path {

    // 路径上的点
    private List<Point> points;

    // 当前所处的位置
    private int nextPos;
}
