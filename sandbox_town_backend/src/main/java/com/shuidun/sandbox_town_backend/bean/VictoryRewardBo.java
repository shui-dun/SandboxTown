package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VictoryRewardBo {
    private List<VictoryItemRewardDo> itemRewards;

    private VictoryAttributeRewardDo attributeReward;

}
