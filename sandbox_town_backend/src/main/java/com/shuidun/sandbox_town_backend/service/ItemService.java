package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.*;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.*;
import com.shuidun.sandbox_town_backend.utils.NameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemService {

    private final SpriteMapper spriteMapper;

    private final SpriteService spriteService;

    private final ItemMapper itemMapper;

    private final ItemTypeLabelMapper itemTypeLabelMapper;

    private final ItemTypeAttributeMapper itemTypeAttributeMapper;

    private final ItemTypeMapper itemTypeMapper;

    private final ItemTypeEffectMapper itemTypeEffectMapper;

    private final EffectMapper effectMapper;

    public ItemService(SpriteMapper spriteMapper, SpriteService spriteService, ItemMapper itemMapper, ItemTypeLabelMapper itemTypeLabelMapper, ItemTypeAttributeMapper itemTypeAttributeMapper, ItemTypeMapper itemTypeMapper, ItemTypeEffectMapper itemTypeEffectMapper, EffectMapper effectMapper) {
        this.spriteMapper = spriteMapper;
        this.spriteService = spriteService;
        this.itemMapper = itemMapper;
        this.itemTypeLabelMapper = itemTypeLabelMapper;
        this.itemTypeAttributeMapper = itemTypeAttributeMapper;
        this.itemTypeMapper = itemTypeMapper;
        this.itemTypeEffectMapper = itemTypeEffectMapper;
        this.effectMapper = effectMapper;
    }

    @Transactional
    public Sprite use(String owner, String itemId) {
        // 判断物品是否存在
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断角色是否拥有该物品
        if (!item.getOwner().equals(owner)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        // 判断物品是否可用
        Set<ItemLabelEnum> labels = itemTypeLabelMapper.selectByItemType(item.getItemType());
        if (!labels.contains(ItemLabelEnum.FOOD) && !labels.contains(ItemLabelEnum.USABLE)) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_USABLE);
        }
        // 得到物品带来的属性变化
        ItemTypeAttribute itemTypeAttribute = itemTypeAttributeMapper.selectByItemTypeAndOperation(item.getItemType(), ItemOperationEnum.USE);
        // TODO: 根据物品等级计算属性变化
        // 得到角色原先属性
        Sprite sprite = spriteMapper.selectById(owner);
        // 更新角色属性
        sprite.setMoney(sprite.getMoney() + itemTypeAttribute.getMoneyInc());
        sprite.setExp(sprite.getExp() + itemTypeAttribute.getExpInc());
        sprite.setHunger(sprite.getHunger() + itemTypeAttribute.getHungerInc());
        sprite.setHp(sprite.getHp() + itemTypeAttribute.getHpInc());
        sprite.setAttack(sprite.getAttack() + itemTypeAttribute.getAttackInc());
        sprite.setDefense(sprite.getDefense() + itemTypeAttribute.getDefenseInc());
        sprite.setSpeed(sprite.getSpeed() + itemTypeAttribute.getSpeedInc());
        // TODO: 向角色施加效果
        // 判断新属性是否在合理范围内（包含升级操作），随后写入数据库
        sprite = spriteService.normalizeAndUpdatePlayer(sprite);
        // 判断是否是最后一个物品
        if (item.getItemCount() <= 1) {
            // 删除物品
            itemMapper.deleteById(item);
        } else {
            // 更新物品数量
            item.setItemCount(item.getItemCount() - 1);
            itemMapper.updateById(item);
        }
        return sprite;
    }

    // 给玩家添加物品
    @Transactional
    public void add(String spriteId, ItemTypeEnum itemTypeId, int count) {
        ItemType itemType = itemTypeMapper.selectById(itemTypeId);
        // 判断物品是否可堆叠
        if (itemType.getDurability() != -1) {
            // 不可堆叠，直接插入
            for (int i = 0; i < count; i++) {
                Item item = new Item();
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
            List<Item> items = itemMapper.selectByOwnerAndItemType(spriteId, itemTypeId);
            if (items == null || items.size() == 0) {
                // 玩家没有该物品，直接插入
                Item item = new Item();
                item.setId(NameGenerator.generateItemName(itemTypeId.name()));
                item.setOwner(spriteId);
                item.setItemType(itemTypeId);
                item.setItemCount(count);
                item.setLife(100);
                item.setLevel(1);
                item.setPosition(ItemPositionEnum.BACKPACK);
                itemMapper.insert(item);
            } else {
                Item item = items.get(0);
                // 玩家有该物品，更新数量
                item.setItemCount(item.getItemCount() + count);
                itemMapper.updateById(item);
            }
        }
    }

    // 根据主人查询物品（带有物品类型信息和标签信息）
    public List<Item> listByOwnerWithTypeAndLabel(String owner) {
        // 找到所有物品
        List<Item> items = itemMapper.selectByOwner(owner);
        // 如果没有物品，直接返回
        if (items == null || items.isEmpty()) {
            return items;
        }
        // 为物品列表设置物品类型信息和标签信息
        setItemTypeAndLabelsForItems(items);
        return items;
    }


    // 根据主人以及位置查询物品（带有物品类型信息和标签信息）
    public List<Item> listItemsByOwnerAndPositionWithTypeAndLabel(String owner, ItemPositionEnum position) {
        // 找到所有物品
        List<Item> items = itemMapper.selectByOwnerAndPosition(owner, position);
        // 如果没有物品，直接返回
        if (items == null || items.isEmpty()) {
            return items;
        }
        // 为物品列表设置物品类型信息和标签信息
        setItemTypeAndLabelsForItems(items);
        return items;
    }

    // 为物品类型列表设置标签信息
    private void setLabelsForItemTypes(List<ItemType> itemTypes) {
        // 找到所有物品类型的标签
        List<ItemTypeLabel> itemTypeLabels = itemTypeLabelMapper.selectByItemTypes(itemTypes.stream().map(ItemType::getId).collect(Collectors.toList()));
        // 根据物品类型id分组
        Map<ItemTypeEnum, Set<ItemTypeLabel>> itemTypeLabelMap = itemTypeLabels.stream().collect(Collectors.groupingBy(ItemTypeLabel::getItemType, Collectors.toSet()));
        // 为每个物品类型设置标签
        itemTypes.forEach(itemType -> itemType.setLabels(itemTypeLabelMap.get(itemType.getId()).stream().map(ItemTypeLabel::getLabel).collect(Collectors.toSet())));
    }

    // 为物品列表设置物品类型信息（带有标签信息）
    private void setItemTypeAndLabelsForItems(List<Item> items) {
        // 找到所有物品类型
        List<ItemType> itemTypes = itemTypeMapper.selectBatchIds(items.stream().map(Item::getItemType).collect(Collectors.toList()));
        // 为物品类型列表设置标签信息
        setLabelsForItemTypes(itemTypes);
        // 根据物品类型id分组
        Map<ItemTypeEnum, ItemType> itemTypeMap = itemTypes.stream().collect(Collectors.toMap(ItemType::getId, itemType -> itemType));
        // 为每个物品设置物品类型信息
        items.forEach(item -> item.setItemTypeObj(itemTypeMap.get(item.getItemType())));
    }

    // 获得物品类型的效果列表
    private Set<ItemTypeEffect> selectEffectsByItemType(ItemTypeEnum itemType) {
        Set<ItemTypeEffect> itemTypeEffects = itemTypeEffectMapper.selectByItemType(itemType);
        // 得到效果的详细信息，例如效果的描述
        Set<EffectEnum> effectEnums = itemTypeEffects.stream().map(ItemTypeEffect::getEffect).collect(Collectors.toSet());
        Map<EffectEnum, Effect> effectMap = effectMapper.selectBatchIds(effectEnums).stream().collect(Collectors.toMap(Effect::getId, effect -> effect));
        itemTypeEffects.forEach(itemTypeEffect -> itemTypeEffect.setEffectObj(effectMap.get(itemTypeEffect.getEffect())));
        return itemTypeEffects;
    }

    // 根据物品类型id查询物品类型详细信息（即包含标签信息、属性增益信息、效果信息）
    public ItemType getItemTypeDetailById(ItemTypeEnum itemTypeId) {
        // 找到物品类型
        ItemType itemType = itemTypeMapper.selectById(itemTypeId);

        // 设置物品类型的标签
        Set<ItemLabelEnum> itemTypeLabels = itemTypeLabelMapper.selectByItemType(itemTypeId);
        itemType.setLabels(itemTypeLabels);

        // 找到物品类型的属性增益
        Set<ItemTypeAttribute> itemTypeAttribute = itemTypeAttributeMapper.selectByItemType(itemTypeId);
        // 将物品品类型的属性增益按照操作类型分组
        Map<ItemOperationEnum, ItemTypeAttribute> itemTypeAttributeMap = itemTypeAttribute.stream().collect(Collectors.toMap(ItemTypeAttribute::getOperation, itemTypeAttribute1 -> itemTypeAttribute1));
        // 设置物品类型的属性增益
        itemType.setAttributes(itemTypeAttributeMap);

        // 找到物品类型的效果
        Set<ItemTypeEffect> itemTypeEffects = selectEffectsByItemType(itemTypeId);
        // 根据操作和效果名称组装成map
        Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffect>> itemTypeEffectMap = itemTypeEffects.stream().collect(Collectors.groupingBy(ItemTypeEffect::getOperation, Collectors.toMap(ItemTypeEffect::getEffect, itemTypeEffect -> itemTypeEffect)));
        // 设置物品类型的效果
        itemType.setEffects(itemTypeEffectMap);

        return itemType;
    }

    // 根据物品id查询物品详细信息（即包含物品类型信息、标签信息、属性增益信息、效果信息）
    public Item getItemDetailById(String itemId) {
        // 找到物品
        Item item = itemMapper.selectById(itemId);
        // 找到物品类型
        ItemType itemType = getItemTypeDetailById(item.getItemType());
        // 设置物品类型
        item.setItemTypeObj(itemType);
        return item;
    }

}
