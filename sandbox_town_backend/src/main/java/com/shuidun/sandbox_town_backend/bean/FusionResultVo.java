package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FusionResultVo {
    private Map<String, Integer> deductedItems; // item id -> deducted quantity
    private ItemTypeEnum resultItem;  // The item that would be created by fusion
}