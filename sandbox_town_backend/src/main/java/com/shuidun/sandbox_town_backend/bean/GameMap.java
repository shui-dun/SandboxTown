package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMap {

    private String id;

    private String name;

    private Integer width;

    private Integer height;

    private Integer seed;
}
