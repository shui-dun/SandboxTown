package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.VictoryRewardBo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.mapper.VictoryAttributeRewardMapper;
import com.shuidun.sandbox_town_backend.mapper.VictoryItemRewardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VictoryRewardService {
    private final VictoryItemRewardMapper victoryItemRewardMapper;

    private final VictoryAttributeRewardMapper victoryAttributeRewardMapper;

    public VictoryRewardService(VictoryItemRewardMapper victoryItemRewardMapper, VictoryAttributeRewardMapper victoryAttributeRewardMapper) {
        this.victoryItemRewardMapper = victoryItemRewardMapper;
        this.victoryAttributeRewardMapper = victoryAttributeRewardMapper;
    }

    @Cacheable(value = "victoryReward::selectBySpriteType")
    public VictoryRewardBo selectBySpriteType(SpriteTypeEnum spriteType) {
        return new VictoryRewardBo(
                victoryItemRewardMapper.selectBySpriteType(spriteType),
                victoryAttributeRewardMapper.selectById(spriteType)
        );
    }
}
