package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemPositionEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.mapper.EffectMapper;
import com.shuidun.sandbox_town_backend.mapper.ItemTypeEffectMapper;
import com.shuidun.sandbox_town_backend.mapper.SpriteEffectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EffectService {
    private final SpriteEffectMapper spriteEffectMapper;

    private final EffectMapper effectMapper;

    private final ItemTypeEffectMapper itemTypeEffectMapper;

    public EffectService(SpriteEffectMapper spriteEffectMapper, EffectMapper effectMapper, ItemTypeEffectMapper itemTypeEffectMapper) {
        this.spriteEffectMapper = spriteEffectMapper;
        this.effectMapper = effectMapper;
        this.itemTypeEffectMapper = itemTypeEffectMapper;
    }

    /**
     * 获取精灵的效果列表
     *
     * @param spriteId   精灵id
     * @param equipments 精灵的装备列表
     */
    public List<SpriteEffectWithEffectBo> listSpriteEffectsBySpriteIdAndEquipments(String spriteId, List<ItemDetailBo> equipments) {
        // 从数据库中获取精灵的效果列表 （但注意这不包含装备的效果）
        Map<EffectEnum, SpriteEffectDo> spriteEffectMap = selectEffectsAndDeleteExpiredEffects(spriteId).stream().collect(Collectors.toMap(SpriteEffectDo::getEffect, Function.identity()));
        // 获得装备的效果列表
        List<ItemTypeEffectDo> equipmentEffectList = new ArrayList<>();
        for (ItemDetailBo item : equipments) {
            // 判断物品的位置
            ItemPositionEnum position = item.getPosition();
            // 如果是手持
            if (position == ItemPositionEnum.HANDHELD) {
                // 使用Optional来避免空指针异常
                // map()函数会对存在的值进行计算，返回一个新的Optional。如果源Optional为空，它将直接返回一个空的Optional
                // ifPresent()函数在Optional值存在时会执行给定的lambda表达式
                Optional.of(item.getItemTypeObj().getEffects())
                        .map(e -> e.get(ItemOperationEnum.HANDHELD))
                        .ifPresent(v -> equipmentEffectList.addAll(v.values()));
            } else { // 如果是装备栏
                Optional.of(item.getItemTypeObj().getEffects())
                        .map(e -> e.get(ItemOperationEnum.EQUIP))
                        .ifPresent(v -> equipmentEffectList.addAll(v.values()));
            }
        }
        // 将装备的效果列表和精灵的效果列表合并
        for (ItemTypeEffectDo equipmentEffect : equipmentEffectList) {
            // 如果精灵的效果列表中没有这个效果
            SpriteEffectDo spriteEffectDo = spriteEffectMap.get(equipmentEffect.getEffect());
            if (spriteEffectDo == null) {
                // 直接加入
                spriteEffectDo = new SpriteEffectDo(
                        spriteId,
                        equipmentEffect.getEffect(),
                        -1, // 装备的效果时效显然是永久
                        -1L
                );
                spriteEffectMap.put(equipmentEffect.getEffect(), spriteEffectDo);
            } else { // 如果精灵的效果列表中有这个效果
                // 装备的效果时效显然是永久
                spriteEffectDo.setDuration(-1);
                spriteEffectDo.setExpire(-1L);
            }
        }
        var ans = new ArrayList<SpriteEffectWithEffectBo>();
        // 添加效果详细信息到spriteEffectMap
        if (!spriteEffectMap.isEmpty()) {
            List<EffectDo> effectList = effectMapper.selectBatchIds(spriteEffectMap.keySet());
            // 按照效果名组织效果列表
            Map<EffectEnum, EffectDo> effectMap = effectList.stream().collect(Collectors.toMap(EffectDo::getId, Function.identity()));
            // 将效果详细信息添加到spriteEffectMap
            for (SpriteEffectDo spriteEffectDo : spriteEffectMap.values()) {
                EffectDo effect = effectMap.get(spriteEffectDo.getEffect());
                assert effect != null;
                ans.add(new SpriteEffectWithEffectBo(spriteEffectDo, effect));
            }
        }
        return ans;
    }

    /**
     * 删除精灵过期的效果并返回所有未过期的效果
     * 注意：这只包含精灵的效果，不包含装备的效果
     */
    private List<SpriteEffectDo> selectEffectsAndDeleteExpiredEffects(String spriteId) {
        List<SpriteEffectDo> effects = spriteEffectMapper.selectBySprite(spriteId);
        List<SpriteEffectDo> unexpiredEffects = new ArrayList<>();
        for (var effect : effects) {
            if (effect.getExpire() != -1 && effect.getExpire() < System.currentTimeMillis()) {
                spriteEffectMapper.deleteBySpriteAndEffect(spriteId, effect.getEffect());
            } else {
                unexpiredEffects.add(effect);
            }
        }
        return unexpiredEffects;
    }

    /**
     * 向精灵施加效果
     *
     * @param spriteId 精灵id
     * @param effectId 效果id
     * @param duration 效果持续时间
     */
    public void addEffect(String spriteId, EffectEnum effectId, int duration) {
        // 精灵原先是否有该效果
        SpriteEffectDo spriteEffect = spriteEffectMapper.selectBySpriteAndEffect(spriteId, effectId);
        if (spriteEffect != null) {
            // 如果效果是永久的
            if (spriteEffect.getDuration() == -1 || duration == -1) {
                spriteEffect.setDuration(-1);
                spriteEffect.setExpire(-1L);
                spriteEffectMapper.update(spriteEffect);
                return;
            } else if (spriteEffect.getExpire() > System.currentTimeMillis()) {
                // 如果有未过期的效果，则更新效果
                spriteEffect.setDuration(spriteEffect.getDuration() + duration);
                spriteEffect.setExpire(spriteEffect.getExpire() + duration * 1000L);
                spriteEffectMapper.update(spriteEffect);
                return;
            }
        }
        // 如果原没有这个效果或者已过期，则添加效果
        spriteEffect = new SpriteEffectDo(
                spriteId,
                effectId,
                duration,
                duration == -1 ? -1L : System.currentTimeMillis() + duration * 1000L
        );
        spriteEffectMapper.insertOrUpdate(spriteEffect);

    }

    /** 获得物品类型的效果列表 */
    public Set<ItemTypeEffectWithEffectBo> selectEffectsByItemType(ItemTypeEnum itemType) {
        Set<ItemTypeEffectDo> itemTypeEffects = new HashSet<>(itemTypeEffectMapper.selectByItemType(itemType));
        // 得到效果的详细信息，例如效果的描述
        Set<EffectEnum> effectEnums = itemTypeEffects.stream().map(ItemTypeEffectDo::getEffect).collect(Collectors.toSet());
        if (effectEnums.isEmpty()) {
            return Collections.emptySet();
        }
        Map<EffectEnum, EffectDo> effectMap = effectMapper.selectBatchIds(effectEnums).stream().collect(Collectors.toMap(EffectDo::getId, effect -> effect));
        return itemTypeEffects.stream().map(itemTypeEffect -> {
            EffectDo effect = effectMap.get(itemTypeEffect.getEffect());
            assert effect != null;
            return new ItemTypeEffectWithEffectBo(itemTypeEffect, effect);
        }).collect(Collectors.toSet());
    }
}
