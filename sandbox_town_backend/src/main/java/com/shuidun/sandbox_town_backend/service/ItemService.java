package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.*;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.ItemMapper;
import com.shuidun.sandbox_town_backend.mapper.ItemTypeAttributeMapper;
import com.shuidun.sandbox_town_backend.mapper.ItemTypeLabelMapper;
import com.shuidun.sandbox_town_backend.mapper.ItemTypeMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.utils.UUIDNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemService {

    /** 物品栏大小 */
    private final int ITEM_BAR_SIZE = 6;

    /**
     * 自注入，使得调用自身的方法时可以走 Spring AOP（使得注解生效）
     * 不加入Lazy会报错：循环依赖
     */
    @Lazy
    @Autowired
    private ItemService self;

    private final ItemMapper itemMapper;

    private final ItemTypeLabelMapper itemTypeLabelMapper;

    private final ItemTypeAttributeMapper itemTypeAttributeMapper;

    private final ItemTypeMapper itemTypeMapper;

    private final EffectService effectService;

    public ItemService(ItemMapper itemMapper, ItemTypeLabelMapper itemTypeLabelMapper, ItemTypeAttributeMapper itemTypeAttributeMapper, ItemTypeMapper itemTypeMapper, EffectService effectService) {
        this.itemMapper = itemMapper;
        this.itemTypeLabelMapper = itemTypeLabelMapper;
        this.itemTypeAttributeMapper = itemTypeAttributeMapper;
        this.itemTypeMapper = itemTypeMapper;
        this.effectService = effectService;
    }

    /** 给玩家添加物品 */
    @Transactional
    public void add(String spriteId, ItemTypeEnum itemTypeId, int count) {
        ItemTypeDo itemType = itemTypeMapper.selectById(itemTypeId);
        assert itemType != null;
        // 判断物品是否可堆叠
        if (itemType.getDurability() != -1) {
            // 不可堆叠，直接插入
            for (int i = 0; i < count; i++) {
                ItemDo item = new ItemDo();
                item.setId(UUIDNameGenerator.generateItemName(itemTypeId.name()));
                item.setOwner(spriteId);
                item.setItemType(itemTypeId);
                item.setItemCount(1);
                item.setLife(Constants.MAX_ITEM_LIFE);
                item.setLevel(1);
                item.setPosition(ItemPositionEnum.BACKPACK);
                itemMapper.insert(item);
            }
        } else {
            // 判断玩家是否拥有该物品
            List<ItemDo> items = itemMapper.selectByOwnerAndItemType(spriteId, itemTypeId);
            if (items.isEmpty()) {
                // 玩家没有该物品，直接插入
                ItemDo item = new ItemDo();
                item.setId(UUIDNameGenerator.generateItemName(itemTypeId.name()));
                item.setOwner(spriteId);
                item.setItemType(itemTypeId);
                item.setItemCount(count);
                item.setLife(Constants.MAX_ITEM_LIFE);
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
    @CacheEvict(value = "item::itemDetail", key = "#itemId")
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
    public List<ItemWithTypeAndLabelsBo> listByOwnerWithTypeAndLabel(String owner) {
        return setItemTypeAndLabelsForItems(itemMapper.selectByOwner(owner));
    }


    /** 根据主人以及位置查询物品（带有物品类型信息和标签信息） */
    public List<ItemWithTypeAndLabelsBo> listItemsByOwnerAndPositionWithTypeAndLabel(String owner, ItemPositionEnum position) {
        return setItemTypeAndLabelsForItems(itemMapper.selectByOwnerAndPosition(owner, position));
    }


    /** 根据主人以及位置查询物品 */
    public List<ItemDo> listItemsByOwnerAndPosition(String owner, ItemPositionEnum position) {
        return itemMapper.selectByOwnerAndPosition(owner, position);
    }


    /** 根据主人查询背包中的物品（带有物品类型信息和标签信息） */
    public List<ItemWithTypeAndLabelsBo> listItemsInBackpackByOwner(String owner) {
        return listItemsByOwnerAndPositionWithTypeAndLabel(owner, ItemPositionEnum.BACKPACK);
    }

    /**
     * 根据主人以及位置列表查询物品（带有物品类型信息和标签信息）
     * 物品的位置在列表中的任意一个即可
     */
    public List<ItemWithTypeAndLabelsBo> listItemsByOwnerAndPositionsWithTypeAndLabel(String owner, List<ItemPositionEnum> positions) {
        return setItemTypeAndLabelsForItems(itemMapper.selectByOwnerAndPositions(owner, positions));
    }

    /**
     * 根据主人查询装备栏中的物品（带有物品类型信息和标签信息）
     * 这里的装备栏还包括手持
     */
    public List<ItemWithTypeAndLabelsBo> listItemsInEquipmentByOwner(String owner) {
        return listItemsByOwnerAndPositionsWithTypeAndLabel(owner, Arrays.asList(ItemPositionEnum.HELMET, ItemPositionEnum.CHEST, ItemPositionEnum.LEG, ItemPositionEnum.BOOTS, ItemPositionEnum.HANDHELD));
    }

    /**
     * 根据主人查询装备栏中的物品（带有物品类型信息、标签信息、属性增益信息、特殊效果信息）
     * 之所以需要查询属性增益信息和特殊效果信息，是因为装备栏中的物品经常需要计算属性增益和特殊效果
     * 注意：这里的装备栏还包括手持
     */
    public List<ItemDetailBo> listItemsInEquipmentByOwnerWithDetail(String owner) {
        // 获得所有装备（包括手持）
        List<ItemWithTypeAndLabelsBo> items = listItemsInEquipmentByOwner(owner);
        // 获得这些装备的属性增量信息等详细信息
        List<ItemDetailBo> detailedItems = new ArrayList<>();
        items.forEach(item -> {
            ItemDetailBo detailedItem = self.getItemDetailById(item.getId());
            detailedItems.add(detailedItem);
        });
        return detailedItems;
    }


    /** 根据主人查询物品栏（包括手持）中的物品（带有物品类型信息和标签信息） */
    public List<ItemWithTypeAndLabelsBo> listItemsInItemBarByOwner(String owner) {
        return listItemsByOwnerAndPositionsWithTypeAndLabel(owner, Arrays.asList(ItemPositionEnum.ITEMBAR, ItemPositionEnum.HANDHELD));
    }

    /** 根据物品类型列表查询物品类型信息（即包含标签信息） */
    public List<ItemTypeWithLabelsBo> listItemTypeWithLabels(List<ItemTypeEnum> itemTypeIds) {
        if (itemTypeIds.isEmpty()) {
            return Collections.emptyList();
        }
        // 找到物品类型
        List<ItemTypeDo> itemTypes = itemTypeMapper.selectBatchIds(itemTypeIds);
        // 为物品类型列表设置标签信息
        return setLabelsForItemTypes(itemTypes);
    }

    /** 为物品类型列表设置标签信息 */
    private List<ItemTypeWithLabelsBo> setLabelsForItemTypes(List<ItemTypeDo> itemTypes) {
        // 找到所有物品类型的标签
        List<ItemTypeLabelDo> itemTypeLabels = itemTypeLabelMapper.selectByItemTypes(itemTypes.stream().map(ItemTypeDo::getId).collect(Collectors.toList()));
        // 根据物品类型id分组
        Map<ItemTypeEnum, Set<ItemTypeLabelDo>> itemTypeLabelMap = itemTypeLabels.stream().collect(Collectors.groupingBy(ItemTypeLabelDo::getItemType, Collectors.toSet()));
        // 为每个物品类型设置标签（如果没有标签，会返回空集合）
        return itemTypes.stream()
                .map(itemType -> new ItemTypeWithLabelsBo(
                        itemType,
                        itemTypeLabelMap.getOrDefault(itemType.getId(), new HashSet<>()).stream().map(ItemTypeLabelDo::getLabel).collect(Collectors.toSet())))
                .collect(Collectors.toList());
    }

    /** 为物品列表设置物品类型信息（带有标签信息） */
    private List<ItemWithTypeAndLabelsBo> setItemTypeAndLabelsForItems(List<ItemDo> items) {
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        // 找到所有物品类型
        List<ItemTypeDo> itemTypes = itemTypeMapper.selectBatchIds(
                items.stream().map(ItemDo::getItemType).collect(Collectors.toList()));
        // 为物品类型列表设置标签信息
        List<ItemTypeWithLabelsBo> itemTypesWithLabels = setLabelsForItemTypes(itemTypes);
        // 根据物品类型id分组
        Map<ItemTypeEnum, ItemTypeWithLabelsBo> itemTypeMap = itemTypesWithLabels.stream()
                .collect(Collectors.toMap(ItemTypeDo::getId, itemType -> itemType));
        // 为每个物品设置物品类型信息
        return items.stream().map(item -> {
            ItemTypeWithLabelsBo itemType = itemTypeMap.get(item.getItemType());
            assert itemType != null;
            return new ItemWithTypeAndLabelsBo(item, itemType);
        }).collect(Collectors.toList());
    }

    /** 根据物品类型id查询物品类型详细信息（即包含标签信息、属性增益信息、效果信息） */
    @Cacheable(value = "item::itemTypeDetail")
    public ItemTypeDetailBo getItemTypeDetailById(ItemTypeEnum itemTypeId) {
        // 找到物品类型
        ItemTypeDo itemType = itemTypeMapper.selectById(itemTypeId);
        assert itemType != null;
        // 设置物品类型的标签
        Set<ItemLabelEnum> itemTypeLabels = itemTypeLabelMapper.selectByItemType(itemTypeId);
        ItemTypeWithLabelsBo itemTypeWithLabelsBo = new ItemTypeWithLabelsBo(itemType, itemTypeLabels);
        // 找到物品类型的属性增益
        List<ItemTypeAttributeDo> itemTypeAttribute = itemTypeAttributeMapper.selectByItemType(itemTypeId);
        // 将物品品类型的属性增益按照操作类型分组
        Map<ItemOperationEnum, ItemTypeAttributeDo> itemTypeAttributeMap = itemTypeAttribute.stream()
                .collect(Collectors.toMap(ItemTypeAttributeDo::getOperation, itemTypeAttribute1 -> itemTypeAttribute1));

        // 找到物品类型的效果
        Set<ItemTypeEffectWithEffectBo> itemTypeEffects = effectService.selectEffectsByItemType(itemTypeId);
        // 根据操作和效果名称组装成map
        Map<ItemOperationEnum, Map<EffectEnum, ItemTypeEffectWithEffectBo>> itemTypeEffectMap = itemTypeEffects.stream()
                .collect(Collectors.groupingBy(ItemTypeEffectDo::getOperation, Collectors.toMap(ItemTypeEffectDo::getEffect, x -> x)));
        // 设置物品类型的效果以及属性增益
        return new ItemTypeDetailBo(itemTypeWithLabelsBo, itemTypeAttributeMap, itemTypeEffectMap);
    }

    /** 根据物品id查询物品详细信息（即包含物品类型信息、标签信息、属性增益信息、效果信息） */
    @Nullable
    @Cacheable(value = "item::itemDetail")
    public ItemDetailBo getItemDetailById(String itemId) {
        // 找到物品
        ItemDo item = itemMapper.selectById(itemId);
        if (item == null) {
            return null;
        }
        // 找到物品类型
        ItemTypeDetailBo itemType = self.getItemTypeDetailById(item.getItemType());
        // 设置物品类型
        return new ItemDetailBo(item, itemType);
    }

    /** 根据物品id查询物品以及物品类型信息 */
    @Nullable
    public ItemWithTypeBo getItemWithTypeById(String itemId) {
        // 找到物品
        ItemDo item = itemMapper.selectById(itemId);
        if (item == null) {
            return null;
        }
        // 找到物品类型
        ItemTypeDo itemType = itemTypeMapper.selectById(item.getItemType());
        assert itemType != null;
        // 设置物品类型
        return new ItemWithTypeBo(item, itemType);
    }

    /** 根据物品id查询物品信息 */
    @Nullable
    public ItemDo getItemById(String itemId) {
        return itemMapper.selectById(itemId);
    }


    public ItemTypeDo getItemTypeBriefById(ItemTypeEnum itemType) {
        ItemTypeDo itemTypeDo = itemTypeMapper.selectById(itemType);
        assert itemTypeDo != null;
        return itemTypeDo;
    }

    /**
     * 手持物品
     */
    @Transactional
    @CacheEvict(value = "item::itemDetail", key = "#itemId")
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
        List<ItemWithTypeAndLabelsBo> itemInItemBar = listItemsInItemBarByOwner(spriteId);
        if (itemInItemBar.size() >= ITEM_BAR_SIZE
                && item.getPosition() != ItemPositionEnum.ITEMBAR) {
            throw new BusinessException(StatusCodeEnum.ITEMBAR_FULL);
        }
        // 将之前的手持物品放入物品栏
        List<ItemDo> handHeldItems = itemMapper.selectByOwnerAndPosition(
                spriteId, ItemPositionEnum.HANDHELD);
        if (!handHeldItems.isEmpty()) {
            ItemDo handHeldItem = handHeldItems.get(0);
            handHeldItem.setPosition(ItemPositionEnum.ITEMBAR);
            itemMapper.updateById(handHeldItem);
        }
        // 将该物品放入手持物品栏
        item.setPosition(ItemPositionEnum.HANDHELD);
        itemMapper.updateById(item);

        responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY,
                new ItemBarNotifyVo(spriteId)));
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE,
                new SpriteEffectChangeVo(spriteId)));

        return responses;
    }

    /**
     * 放入物品栏
     */
    @Transactional
    @CacheEvict(value = "item::itemDetail", key = "#itemId")
    public List<WSResponseVo> putInItemBar(String spriteId, String itemId) {
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
        List<ItemWithTypeAndLabelsBo> itemInItemBar = listItemsInItemBarByOwner(spriteId);
        if (itemInItemBar.size() >= ITEM_BAR_SIZE
                && item.getPosition() != ItemPositionEnum.HANDHELD) {
            throw new BusinessException(StatusCodeEnum.ITEMBAR_FULL);
        }
        // 将该物品放入物品栏
        item.setPosition(ItemPositionEnum.ITEMBAR);
        itemMapper.updateById(item);

        responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY,
                new ItemBarNotifyVo(spriteId)));
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE,
                new SpriteEffectChangeVo(spriteId)));

        return responses;
    }

    /** 装备物品 */
    @Transactional
    @CacheEvict(value = "item::itemDetail", key = "#itemId")
    public List<WSResponseVo> equip(String spriteId, String itemId) {
        List<WSResponseVo> responses = new ArrayList<>();
        // 查询该物品详细信息
        ItemDetailBo item = self.getItemDetailById(itemId);
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
        ItemPositionEnum originalPosition = item.getPosition();
        if (equipmentPositions.contains(originalPosition)) {
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
        List<ItemDo> equippedItems = itemMapper.selectByOwnerAndPosition(
                spriteId, itemPosition);
        if (!equippedItems.isEmpty()) {
            ItemDo equippedItem = equippedItems.get(0);
            equippedItem.setPosition(ItemPositionEnum.BACKPACK);
            itemMapper.updateById(equippedItem);
        }
        // 将该物品装备
        item.setPosition(itemPosition);
        itemMapper.updateById(item);

        // 如果原先在物品栏，发送物品栏通知
        if (originalPosition == ItemPositionEnum.ITEMBAR || originalPosition == ItemPositionEnum.HANDHELD) {
            responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, new ItemBarNotifyVo(spriteId)));
        }
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(spriteId)));

        return responses;
    }

    /** 放入背包 */
    @Transactional
    @CacheEvict(value = "item::itemDetail", key = "#itemId")
    public List<WSResponseVo> putInBackpack(String spriteId, String itemId) {
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
            responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, new ItemBarNotifyVo(spriteId)));
        }
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(spriteId)));

        return responses;
    }

    /** 获取所有物品类型 */
    public List<ItemTypeDo> listAllItemTypes() {
        return itemTypeMapper.selectList(null);
    }
}
