package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemLabelEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemOperationEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.mapper.ItemTypeAttributeMapper;
import com.shuidun.sandbox_town_backend.mapper.ItemTypeLabelMapper;
import com.shuidun.sandbox_town_backend.mapper.ItemTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemTypeService {
    private final ItemTypeMapper itemTypeMapper;

    private final ItemTypeLabelMapper itemTypeLabelMapper;

    private final ItemTypeAttributeMapper itemTypeAttributeMapper;

    private final EffectService effectService;

    public ItemTypeService(ItemTypeMapper itemTypeMapper, ItemTypeLabelMapper itemTypeLabelMapper, ItemTypeAttributeMapper itemTypeAttributeMapper, EffectService effectService) {
        this.itemTypeMapper = itemTypeMapper;
        this.itemTypeLabelMapper = itemTypeLabelMapper;
        this.itemTypeAttributeMapper = itemTypeAttributeMapper;
        this.effectService = effectService;
    }

    /** 获取所有物品类型 */
    @Cacheable(value = "itemType::allItemTypes")
    public List<ItemTypeEnum> listAllItemTypes() {
        return itemTypeMapper.selectList(null).stream()
                .map(ItemTypeDo::getId)
                .toList();
    }

    /** 根据物品类型id查询物品类型详细信息（即包含标签信息、属性增益信息、效果信息） */
    @Cacheable(value = "itemType::itemType", key = "#itemTypeId")
    public ItemTypeBo getItemTypeById(ItemTypeEnum itemTypeId) {
        // 找到物品类型
        ItemTypeDo itemType = itemTypeMapper.selectById(itemTypeId);
        assert itemType != null;
        // 设置物品类型的标签
        Set<ItemLabelEnum> itemTypeLabels = itemTypeLabelMapper.selectByItemType(itemTypeId);
        // 找到物品类型的属性增益
        List<ItemTypeAttributeDo> itemTypeAttribute = itemTypeAttributeMapper.selectByItemType(itemTypeId);
        // 将物品品类型的属性增益按照操作类型分组
        Map<ItemOperationEnum, ItemTypeAttributeDo> itemTypeAttributeMap = itemTypeAttribute.stream()
                .collect(Collectors.toMap(ItemTypeAttributeDo::getOperation, itemTypeAttribute1 -> itemTypeAttribute1));
        // 找到物品类型的效果
        Set<ItemTypeEffectBo> itemTypeEffects = effectService.selectEffectsByItemType(itemTypeId);
        // 根据操作和效果名称组装成map
        Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffectBo>> itemTypeEffectMap = itemTypeEffects.stream()
                .collect(Collectors.groupingBy(ItemTypeEffectDo::getOperation, Collectors.toMap(ItemTypeEffectDo::getEffect, x -> x)));
        // 设置物品类型的效果以及属性增益
        return new ItemTypeBo(itemType, itemTypeLabels, itemTypeAttributeMap, itemTypeEffectMap);
    }
}
