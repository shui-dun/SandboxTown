package com.shuidun.sandbox_town_backend.bean;

import lombok.Data;
import java.util.Map;

@Data
public class FusionRequestDto {
    private Map<String, Integer> items;  // item id -> quantity
}