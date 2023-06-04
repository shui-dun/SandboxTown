package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Building {

    private String id;

    private String type;

    private String map;

    private Integer level;

    private String owner;

    private Integer originX;

    private Integer originY;

    private Integer displayWidth;

    private Integer displayHeight;
}
