package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.*;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.ItemMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.utils.UUIDNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private final ItemTypeService itemTypeService;

    private final RedisTemplate<String, Object> redisTemplate;

    public ItemService(ItemMapper itemMapper, ItemTypeService itemTypeService, RedisTemplate<String, Object> redisTemplate) {
        this.itemMapper = itemMapper;
        this.itemTypeService = itemTypeService;
        this.redisTemplate = redisTemplate;
    }

    /** 根据物品id查询物品基本信息 */
    @Nullable
    @Cacheable(value = "item::itemDetail", key = "#itemId")
    public ItemDo getItemById(String itemId) {
        return itemMapper.selectById(itemId);
    }

    /** 根据物品id查询物品详细信息（即包含物品类型信息、标签信息、属性增益信息、效果信息） */
    @Nullable
    public ItemDetailBo getItemDetailById(String itemId) {
        // 找到物品
        ItemDo item = self.getItemById(itemId);
        if (item == null) {
            return null;
        }
        // 找到物品类型
        ItemTypeDetailBo itemType = itemTypeService.getItemTypeById(item.getItemType());
        // 设置物品类型
        return new ItemDetailBo(item, itemType);
    }

    /** 根据主人查询物品 */
    // todo: 这里我简单的cache了所有物品，未来可能只cache装备区和物品栏的物品（热点物品）
    @Cacheable(value = "item::listByOwner", key = "#owner")
    public List<String> listByOwner(String owner) {
        List<ItemDo> items = itemMapper.selectByOwner(owner);
        for (ItemDo item : items) {
            redisTemplate.opsForValue().set("item::itemDetail::" + item.getId(), item);
        }
        return items.stream().map(ItemDo::getId).toList();
    }

    /**
     * 根据主人以及位置列表查询物品
     * 物品的位置在列表中的任意一个即可
     */
    public List<ItemDetailBo> listByOwnerAndPositions(String owner, List<ItemPositionEnum> positions) {
        return self.listByOwner(owner)
                .stream()
                .map(self::getItemDetailById)
                .filter(item -> positions.contains(item.getPosition()))
                .toList();
    }

    /**
     * 根据主人查询装备栏中的物品
     * 注意：这里的装备栏还包括手持
     */
    public List<ItemDetailBo> listItemsInEquipmentByOwner(String owner) {
        return listByOwnerAndPositions(owner,
                Arrays.asList(
                        ItemPositionEnum.HELMET,
                        ItemPositionEnum.CHEST,
                        ItemPositionEnum.LEG,
                        ItemPositionEnum.BOOTS,
                        ItemPositionEnum.HANDHELD
                )
        );
    }

    /** 根据主人查询物品栏（包括手持）中的物品 */
    public List<ItemDetailBo> listItemsInItemBarByOwner(String owner) {
        return listByOwnerAndPositions(owner, Arrays.asList(
                ItemPositionEnum.ITEMBAR, ItemPositionEnum.HANDHELD
        ));
    }

    /** 更新物品 */
    @Transactional
    @CacheEvict(value = "item::itemDetail", key = "#item.id")
    public void updateItem(ItemDo item) {
        if (item.getItemCount() == 0) {
            itemMapper.deleteById(item);
        } else {
            itemMapper.updateById(item);
        }
    }

    /** 添加物品 */
    @Transactional
    @Cacheable(value = "item::itemDetail", key = "#item.id")
    public void addItem(ItemDo item) {
        itemMapper.insert(item);
    }

    /** 装备物品 */
    @Transactional
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
        List<ItemDetailBo> equippedItems = listByOwnerAndPositions(spriteId, List.of(itemPosition));
        if (!equippedItems.isEmpty()) {
            ItemDo equippedItem = equippedItems.get(0);
            equippedItem.setPosition(ItemPositionEnum.BACKPACK);
            self.updateItem(equippedItem);
        }
        // 将该物品装备
        item.setPosition(itemPosition);
        self.updateItem(item);

        // 如果原先在物品栏，发送物品栏通知
        if (originalPosition == ItemPositionEnum.ITEMBAR || originalPosition == ItemPositionEnum.HANDHELD) {
            responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, new ItemBarNotifyVo(spriteId)));
        }
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(spriteId)));

        return responses;
    }


    /**
     * 变更物品位置变更
     *
     * @param spriteId       精灵ID
     * @param itemId         物品ID
     * @param targetPosition 目标位置，注意不支持装备位置，装备参见 {@link #equip}
     */
    @Transactional
    public List<WSResponseVo> changeItemPosition(String spriteId, String itemId, ItemPositionEnum targetPosition) {
        List<WSResponseVo> responses = new ArrayList<>();

        // 物品是否存在
        ItemDetailBo itemDetail = self.getItemDetailById(itemId);
        if (itemDetail == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 校验物品归属
        if (!itemDetail.getOwner().equals(spriteId)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        // 原先物品位置
        ItemPositionEnum originalPosition = itemDetail.getPosition();
        // 物品栏信息
        List<ItemDetailBo> itemBarItems = listItemsInItemBarByOwner(spriteId);
        // 手持的物品
        ItemDetailBo handHeldItem = listByOwnerAndPositions(spriteId,
                List.of(ItemPositionEnum.HANDHELD)).stream()
                .findFirst().orElse(null);
        // 若目标位置和当前相同，直接返回
        if (originalPosition == targetPosition) {
            return responses;
        }
        switch (targetPosition) {
            case HANDHELD:
                // 先检查物品栏是否已满
                if (itemBarItems.size() >= ITEM_BAR_SIZE
                        && originalPosition != ItemPositionEnum.ITEMBAR) {
                    throw new BusinessException(StatusCodeEnum.ITEMBAR_FULL);
                }
                if (handHeldItem != null) {
                    handHeldItem.setPosition(ItemPositionEnum.ITEMBAR);
                    // 更新老的手持物品到物品栏
                    self.updateItem(handHeldItem);
                }
                // 最后把当前物品放到手持
                itemDetail.setPosition(ItemPositionEnum.HANDHELD);
                self.updateItem(itemDetail);
                break;
            case ITEMBAR:
                // 判断物品栏是否已满
                if (itemBarItems.size() >= ITEM_BAR_SIZE
                        && originalPosition != ItemPositionEnum.HANDHELD) {
                    throw new BusinessException(StatusCodeEnum.ITEMBAR_FULL);
                }
                // 放入物品栏
                itemDetail.setPosition(ItemPositionEnum.ITEMBAR);
                self.updateItem(itemDetail);
                break;
            case BACKPACK:
                itemDetail.setPosition(ItemPositionEnum.BACKPACK);
                self.updateItem(itemDetail);
                break;
            default:
                throw new BusinessException(StatusCodeEnum.PARAMETER_ERROR);
        }
        // 可能触发精灵效果变动
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE,
                new SpriteEffectChangeVo(spriteId)));
        // 有可能物品栏物品变化了
        responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY,
                new ItemBarNotifyVo(spriteId)));
        return responses;
    }

    /** 给玩家添加物品 */
    @Transactional
    public void add(String spriteId, ItemTypeEnum itemTypeId, int count) {
        ItemTypeDo itemType = itemTypeService.getItemTypeById(itemTypeId);
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
                self.addItem(item);
            }
        } else {
            // 判断玩家是否拥有该物品
            List<String> items = listByOwner(spriteId).stream()
                    .filter(itemID -> itemTypeIs(itemID, itemTypeId))
                    .toList();
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
                self.addItem(item);
            } else {
                ItemDo item = self.getItemById(items.getFirst());
                // 玩家有该物品，更新数量
                item.setItemCount(item.getItemCount() + count);
                updateItem(item);
            }
        }
    }

    /** 给角色减少物品 */
    @Transactional
    public void reduce(String spriteId, String itemId, int count) {
        // 查询玩家拥有的该物品
        ItemDo item = self.getItemById(itemId);
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
        item.setItemCount(item.getItemCount() - count);
        self.updateItem(item);
    }

    /** 判断某个物品是否是某类型 */
    public boolean itemTypeIs(String itemId, ItemTypeEnum itemType) {
        return itemId.startsWith(itemType + "_");
    }
}
