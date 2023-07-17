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
    public List<WSResponseVo> use(String owner, String itemId) {
        List<WSResponseVo> responseList = new ArrayList<>();
        // 判断物品是否存在
        ItemDo item = itemMapper.selectById(itemId);
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
        ItemTypeAttributeDo itemTypeAttribute = itemTypeAttributeMapper.selectByItemTypeAndOperation(item.getItemType(), ItemOperationEnum.USE);
        // TODO: 根据物品等级计算属性变化
        // 得到角色原先属性
        SpriteDo sprite = spriteMapper.selectById(owner);
        SpriteAttributeChangeVo spriteAttributeChange = new SpriteAttributeChangeVo();
        spriteAttributeChange.setOriginal(sprite);
        // 更新角色属性
        sprite.setMoney(sprite.getMoney() + itemTypeAttribute.getMoneyInc());
        sprite.setExp(sprite.getExp() + itemTypeAttribute.getExpInc());
        sprite.setHunger(sprite.getHunger() + itemTypeAttribute.getHungerInc());
        sprite.setHp(sprite.getHp() + itemTypeAttribute.getHpInc());
        sprite.setAttack(sprite.getAttack() + itemTypeAttribute.getAttackInc());
        sprite.setDefense(sprite.getDefense() + itemTypeAttribute.getDefenseInc());
        sprite.setSpeed(sprite.getSpeed() + itemTypeAttribute.getSpeedInc());
        // 判断新属性是否在合理范围内（包含升级操作），随后写入数据库
        sprite = spriteService.normalizeAndUpdatePlayer(sprite);
        if (spriteAttributeChange.setChanged(sprite)) {
            responseList.add(new WSResponseVo(WSResponseEnum.SPRITE_ATTRIBUTE_CHANGE, spriteAttributeChange));
        }
        // TODO: 向角色施加效果
        // 判断是否是最后一个物品
        if (item.getItemCount() <= 1) {
            // 删除物品
            itemMapper.deleteById(item);
        } else {
            // 更新物品数量
            item.setItemCount(item.getItemCount() - 1);
            itemMapper.updateById(item);
        }
        return responseList;
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

    /**
     * 手持物品
     */
    @Transactional
    public List<WSResponseVo> hold(String spriteId, String itemId) {
        List<WSResponseVo> responses = new ArrayList<>();
        // 查询该物品
        ItemDo item = itemMapper.selectById(itemId);
        // 判断该物品是否存在
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断该物品是否属于该精灵
        if (!item.getOwner().equals(spriteId)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        // 判断该物品是否已经是手持物品
        if (item.getPosition() == ItemPositionEnum.HANDHELD) {
            return responses;
        }
        // 判断物品栏是否已满
        List<ItemDo> itemInItemBar = listItemsInItemBarByOwner(spriteId);
        if (itemInItemBar.size() >= Constants.ITEM_BAR_SIZE && item.getPosition() != ItemPositionEnum.ITEMBAR) {
            throw new BusinessException(StatusCodeEnum.ITEMBAR_FULL);
        }
        // 将之前的手持物品放入物品栏
        List<ItemDo> handHeldItems = itemMapper.selectByOwnerAndPosition(spriteId, ItemPositionEnum.HANDHELD);
        if (handHeldItems != null && !handHeldItems.isEmpty()) {
            ItemDo handHeldItem = handHeldItems.get(0);
            handHeldItem.setPosition(ItemPositionEnum.ITEMBAR);
            itemMapper.updateById(handHeldItem);
        }
        // 将该物品放入手持物品栏
        item.setPosition(ItemPositionEnum.HANDHELD);
        itemMapper.updateById(item);

        responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, listItemsInItemBarByOwner(spriteId)));
        return responses;
    }

    /**
     * 放入物品栏
     */
    @Transactional
    public Iterable<WSResponseVo> putInItemBar(String spriteId, String itemId) {
        List<WSResponseVo> responses = new ArrayList<>();
        // 查询该物品
        ItemDo item = itemMapper.selectById(itemId);
        // 判断该物品是否存在
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断该物品是否属于该精灵
        if (!item.getOwner().equals(spriteId)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        // 判断该物品是否已经在物品栏
        if (item.getPosition() == ItemPositionEnum.ITEMBAR) {
            return responses;
        }
        // 判断物品栏是否已满
        List<ItemDo> itemInItemBar = listItemsInItemBarByOwner(spriteId);
        if (itemInItemBar.size() >= Constants.ITEM_BAR_SIZE && item.getPosition() != ItemPositionEnum.HANDHELD) {
            throw new BusinessException(StatusCodeEnum.ITEMBAR_FULL);
        }
        // 将该物品放入物品栏
        item.setPosition(ItemPositionEnum.ITEMBAR);
        itemMapper.updateById(item);

        responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, listItemsInItemBarByOwner(spriteId)));
        return responses;
    }

    /** 装备物品 */
    @Transactional
    public Iterable<WSResponseVo> equip(String spriteId, String itemId) {
        List<WSResponseVo> responses = new ArrayList<>();
        // 查询该物品详细信息
        ItemDo item = getItemDetailById(itemId);
        // 判断该物品是否存在
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断该物品是否属于该精灵
        if (!item.getOwner().equals(spriteId)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        // 装备的所有可能位置
        List<ItemPositionEnum> equipmentPositions = Arrays.asList(
                ItemPositionEnum.HELMET, ItemPositionEnum.CHEST,
                ItemPositionEnum.LEG, ItemPositionEnum.BOOTS);
        // 装备的所有可能标签
        List<ItemLabelEnum> equipmentLabels = Arrays.asList(
                ItemLabelEnum.HELMET, ItemLabelEnum.CHEST,
                ItemLabelEnum.LEG, ItemLabelEnum.BOOTS);
        // 判断该物品是否已经装备
        if (equipmentPositions.contains(item.getPosition())) {
            return responses;
        }
        // 判断该物品是否是装备（判断labels是否包含数组equipmentLabels的任一元素）
        ItemLabelEnum itemLabel = item.getItemTypeObj().getLabels().stream()
                .filter(equipmentLabels::contains)
                .findFirst()
                .orElse(null);
        if (itemLabel == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_EQUIPMENT);
        }
        // 找到该物品的装备位置
        ItemPositionEnum itemPosition = ItemPositionEnum.valueOf(itemLabel.name());
        // 将之前的装备放入背包
        List<ItemDo> equippedItems = itemMapper.selectByOwnerAndPosition(spriteId, itemPosition);
        if (equippedItems != null && !equippedItems.isEmpty()) {
            ItemDo equippedItem = equippedItems.get(0);
            equippedItem.setPosition(ItemPositionEnum.BACKPACK);
            itemMapper.updateById(equippedItem);
        }
        // 将该物品装备
        item.setPosition(itemPosition);
        itemMapper.updateById(item);

        return responses;
    }

    /** 放入背包 */
    @Transactional
    public Iterable<WSResponseVo> putInBackpack(String spriteId, String itemId) {
        List<WSResponseVo> responses = new ArrayList<>();
        // 查询该物品
        ItemDo item = itemMapper.selectById(itemId);
        // 判断该物品是否存在
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断该物品是否属于该精灵
        if (!item.getOwner().equals(spriteId)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        ItemPositionEnum originalPosition = item.getPosition();
        // 判断该物品是否已经在背包
        if (originalPosition == ItemPositionEnum.BACKPACK) {
            return responses;
        }
        // 将该物品放入背包
        item.setPosition(ItemPositionEnum.BACKPACK);
        itemMapper.updateById(item);

        // 如果原先在物品栏，发射物品栏通知
        if (originalPosition == ItemPositionEnum.ITEMBAR || originalPosition == ItemPositionEnum.HANDHELD) {
            responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, listItemsInItemBarByOwner(spriteId)));
        }
        return responses;
    }
}
