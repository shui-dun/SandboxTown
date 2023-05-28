package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Player {

    private String username;
    private long money;
    private long exp;
    private long level;
    private long hunger;
    private long hp;
    private long attack;
    private long defense;
    private long speed;

}