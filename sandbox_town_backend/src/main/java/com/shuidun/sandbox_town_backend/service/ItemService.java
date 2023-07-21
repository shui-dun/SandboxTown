package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.*;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.*;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.utils.NameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemService {

    private final SpriteMapper spriteMapper;
    private final ItemMapper itemMapper;

    private final ItemTypeLabelMapper itemTypeLabelMapper;

    private final ItemTypeAttributeMapper itemTypeAttributeMapper;

    private final ItemTypeMapper itemTypeMapper;

    private final ItemTypeEffectMapper itemTypeEffectMapper;

    private final EffectMapper effectMapper;

    public ItemService(SpriteMapper spriteMapper, ItemMapper itemMapper, ItemTypeLabelMapper itemTypeLabelMapper, ItemTypeAttributeMapper itemTypeAttributeMapper, ItemTypeMapper itemTypeMapper, ItemTypeEffectMapper itemTypeEffectMapper, EffectMapper effectMapper) {
        this.spriteMapper = spriteMapper;
        this.itemMapper = itemMapper;
        this.itemTypeLabelMapper = itemTypeLabelMapper;
        this.itemTypeAttributeMapper = itemTypeAttributeMapper;
        this.itemTypeMapper = itemTypeMapper;
        this.itemTypeEffectMapper = itemTypeEffectMapper;
        this.effectMapper = effectMapper;
    }

    /** 给玩家添加物品 */
    @Transactional
    public void add(String spriteId, ItemTypeEnum itemTypeId, int count) {
        ItemTypeDo itemType = itemTypeMapper.selectById(itemTypeId);
        // 判断物品是否可堆叠
        if (itemType.getDurability() != -1) {
            // 不可堆叠，直接插入
            for (int i = 0; i < count; i++) {
                ItemDo item = new ItemDo();
                item.setId(NameGenerator.generateItemName(itemTypeId.name()));
                item.setOwner(spriteId);
                item.setItemType(itemTypeId);
                item.setItemCount(1);
                item.setLife(100);
                item.setLevel(1);
                item.setPosition(ItemPositionEnum.BACKPACK);
                itemMapper.insert(item);
            }
        } else {
            // 判断玩家是否拥有该物品
            List<ItemDo> items = itemMapper.selectByOwnerAndItemType(spriteId, itemTypeId);
            if (items == null || items.size() == 0) {
                // 玩家没有该物品，直接插入
                ItemDo item = new ItemDo();
                item.setId(NameGenerator.generateItemName(itemTypeId.name()));
                item.setOwner(spriteId);
                item.setItemType(itemTypeId);
                item.setItemCount(count);
                item.setLife(100);
                item.setLevel(1);
                item.setPosition(ItemPositionEnum.BACKPACK);
                itemMapper.insert(item);
            } else {
                ItemDo item = items.get(0);
                // 玩家有该物品，更新数量
                item.setItemCount(item.getItemCount() + count);
                itemMapper.updateById(item);
            }
        }
    }

    /** 给角色减少物品 */
    @Transactional
    public void reduce(String spriteId, String itemId, int count) {
        // 查询玩家拥有的该物品
        ItemDo item = itemMapper.selectById(itemId);
        // 判断物品是否存在
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断物品是否属于该玩家
        if (!item.getOwner().equals(spriteId)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        // 判断物品数量是否足够
        if (item.getItemCount() < count) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_ENOUGH);
        }
        // 判断减少后是否为0
        if (item.getItemCount() == count) {
            // 物品数量为0，直接删除
            itemMapper.deleteById(item);
        } else {
            // 物品数量不为0，更新数量
            item.setItemCount(item.getItemCount() - count);
            itemMapper.updateById(item);
        }
    }

    /** 根据主人查询物品（带有物品类型信息和标签信息） */
    public List<ItemDo> listByOwnerWithTypeAndLabel(String owner) {
        // 找到所有物品
        List<ItemDo> items = itemMapper.selectByOwner(owner);
        // 如果没有物品，直接返回
        if (items == null || items.isEmpty()) {
            return items;
        }
        // 为物品列表设置物品类型信息和标签信息
        setItemTypeAndLabelsForItems(items);
        return items;
    }


    /** 根据主人以及位置查询物品（带有物品类型信息和标签信息） */
    public List<ItemDo> listItemsByOwnerAndPositionWithTypeAndLabel(String owner, ItemPositionEnum position) {
        // 找到所有物品
        List<ItemDo> items = itemMapper.selectByOwnerAndPosition(owner, position);
        // 如果没有物品，直接返回
        if (items == null || items.isEmpty()) {
            return items;
        }
        // 为物品列表设置物品类型信息和标签信息
        setItemTypeAndLabelsForItems(items);
        return items;
    }

    /** 根据主人查询背包中的物品（带有物品类型信息和标签信息） */
    public List<ItemDo> listItemsInBackpackByOwner(String owner) {
        return listItemsByOwnerAndPositionWithTypeAndLabel(owner, ItemPositionEnum.BACKPACK);
    }

    /**
     * 根据主人以及位置列表查询物品（带有物品类型信息和标签信息）
     * 物品的位置在列表中的任意一个即可
     */
    public List<ItemDo> listItemsByOwnerAndPositionsWithTypeAndLabel(String owner, List<ItemPositionEnum> positions) {
        // 找到所有物品
        List<ItemDo> items = itemMapper.selectByOwnerAndPositions(owner, positions);
        // 如果没有物品，直接返回
        if (items == null || items.isEmpty()) {
            return items;
        }
        // 为物品列表设置物品类型信息和标签信息
        setItemTypeAndLabelsForItems(items);
        return items;
    }

    /**
     * 根据主人查询装备栏中的物品（带有物品类型信息和标签信息）
     * 这里的装备栏还包括手持
     */
    public List<ItemDo> listItemsInEquipmentByOwner(String owner) {
        return listItemsByOwnerAndPositionsWithTypeAndLabel(owner, Arrays.asList(ItemPositionEnum.HELMET, ItemPositionEnum.CHEST, ItemPositionEnum.LEG, ItemPositionEnum.BOOTS, ItemPositionEnum.HANDHELD));
    }

    /**
     * 根据主人查询装备栏中的物品（带有物品类型信息、标签信息、属性增益信息、特殊效果信息）
     * 之所以需要查询属性增益信息和特殊效果信息，是因为装备栏中的物品经常需要计算属性增益和特殊效果
     * 这里的装备栏还包括手持
     */
    public List<ItemDo> listItemsInEquipmentByOwnerWithDetail(String owner) {
        // 获得所有装备（包括手持）
        List<ItemDo> items = listItemsInEquipmentByOwner(owner);
        // 获得这些装备的属性增量信息等详细信息
        List<ItemDo> detailedItems = new ArrayList<>();
        items.forEach(item -> {
            ItemDo detailedItem = getItemDetailById(item.getId());
            detailedItems.add(detailedItem);
        });
        return detailedItems;
    }


    /** 根据主人查询物品栏（包括手持）中的物品（带有物品类型信息和标签信息） */
    public List<ItemDo> listItemsInItemBarByOwner(String owner) {
        return listItemsByOwnerAndPositionsWithTypeAndLabel(owner, Arrays.asList(ItemPositionEnum.ITEMBAR, ItemPositionEnum.HANDHELD));
    }

    /** 为物品类型列表设置标签信息 */
    public void setLabelsForItemTypes(Collection<ItemTypeDo> itemTypes) {
        // 找到所有物品类型的标签
        List<ItemTypeLabelDo> itemTypeLabels = itemTypeLabelMapper.selectByItemTypes(itemTypes.stream().map(ItemTypeDo::getId).collect(Collectors.toList()));
        // 根据物品类型id分组
        Map<ItemTypeEnum, Set<ItemTypeLabelDo>> itemTypeLabelMap = itemTypeLabels.stream().collect(Collectors.groupingBy(ItemTypeLabelDo::getItemType, Collectors.toSet()));
        // 为每个物品类型设置标签（如果没有标签，会返回空集合）
        itemTypes.forEach(itemType -> itemType.setLabels(itemTypeLabelMap.getOrDefault(itemType.getId(), new HashSet<>()).stream().map(ItemTypeLabelDo::getLabel).collect(Collectors.toSet())));
    }

    /** 为物品列表设置物品类型信息（带有标签信息） */
    private void setItemTypeAndLabelsForItems(List<ItemDo> items) {
        // 找到所有物品类型
        List<ItemTypeDo> itemTypes = itemTypeMapper.selectBatchIds(items.stream().map(ItemDo::getItemType).collect(Collectors.toList()));
        // 为物品类型列表设置标签信息
        setLabelsForItemTypes(itemTypes);
        // 根据物品类型id分组
        Map<ItemTypeEnum, ItemTypeDo> itemTypeMap = itemTypes.stream().collect(Collectors.toMap(ItemTypeDo::getId, itemType -> itemType));
        // 为每个物品设置物品类型信息
        items.forEach(item -> item.setItemTypeObj(itemTypeMap.get(item.getItemType())));
    }

    /** 获得物品类型的效果列表 */
    private Set<ItemTypeEffectDo> selectEffectsByItemType(ItemTypeEnum itemType) {
        Set<ItemTypeEffectDo> itemTypeEffects = itemTypeEffectMapper.selectByItemType(itemType);
        // 得到效果的详细信息，例如效果的描述
        Set<EffectEnum> effectEnums = itemTypeEffects.stream().map(ItemTypeEffectDo::getEffect).collect(Collectors.toSet());
        if (effectEnums.isEmpty()) {
            return itemTypeEffects;
        }
        Map<EffectEnum, EffectDo> effectMap = effectMapper.selectBatchIds(effectEnums).stream().collect(Collectors.toMap(EffectDo::getId, effect -> effect));
        itemTypeEffects.forEach(itemTypeEffect -> itemTypeEffect.setEffectObj(effectMap.get(itemTypeEffect.getEffect())));
        return itemTypeEffects;
    }

    /** 根据物品类型id查询物品类型详细信息（即包含标签信息、属性增益信息、效果信息） */
    public ItemTypeDo getItemTypeDetailById(ItemTypeEnum itemTypeId) {
        // 找到物品类型
        ItemTypeDo itemType = itemTypeMapper.selectById(itemTypeId);

        // 设置物品类型的标签
        Set<ItemLabelEnum> itemTypeLabels = itemTypeLabelMapper.selectByItemType(itemTypeId);
        itemType.setLabels(itemTypeLabels);

        // 找到物品类型的属性增益
        Set<ItemTypeAttributeDo> itemTypeAttribute = itemTypeAttributeMapper.selectByItemType(itemTypeId);
        // 将物品品类型的属性增益按照操作类型分组
        Map<ItemOperationEnum, ItemTypeAttributeDo> itemTypeAttributeMap = itemTypeAttribute.stream().collect(Collectors.toMap(ItemTypeAttributeDo::getOperation, itemTypeAttribute1 -> itemTypeAttribute1));
        // 设置物品类型的属性增益
        itemType.setAttributes(itemTypeAttributeMap);

        // 找到物品类型的效果
        Set<ItemTypeEffectDo> itemTypeEffects = selectEffectsByItemType(itemTypeId);
        // 根据操作和效果名称组装成map
        Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffectDo>> itemTypeEffectMap = itemTypeEffects.stream().collect(Collectors.groupingBy(ItemTypeEffectDo::getOperation, Collectors.toMap(ItemTypeEffectDo::getEffect, itemTypeEffect -> itemTypeEffect)));
        // 设置物品类型的效果
        itemType.setEffects(itemTypeEffectMap);

        return itemType;
    }

    /** 根据物品id查询物品详细信息（即包含物品类型信息、标签信息、属性增益信息、效果信息） */
    public ItemDo getItemDetailById(String itemId) {
        // 找到物品
        ItemDo item = itemMapper.selectById(itemId);
        // 找到物品类型
        ItemTypeDo itemType = getItemTypeDetailById(item.getItemType());
        // 设置物品类型
        item.setItemTypeObj(itemType);
        return item;
    }

    /** 根据物品id查询物品以及物品类型信息 */
    public ItemDo getItemWithTypeById(String itemId) {
        // 找到物品
        ItemDo item = itemMapper.selectById(itemId);
        // 找到物品类型
        ItemTypeDo itemType = itemTypeMapper.selectById(item.getItemType());
        // 设置物品类型
        item.setItemTypeObj(itemType);
        return item;
    }


}
