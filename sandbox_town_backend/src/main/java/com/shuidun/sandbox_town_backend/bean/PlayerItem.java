package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerItem {
    String owner;
    String itemId;
    int itemCount;
    String name;
    String description;
    int basicPrice;
}
