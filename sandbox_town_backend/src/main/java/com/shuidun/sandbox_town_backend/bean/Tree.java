package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tree {

    private String id;

    private Integer applesCount;

    private Integer maxApplesCount;

    private Integer limitPerSprite;
}
