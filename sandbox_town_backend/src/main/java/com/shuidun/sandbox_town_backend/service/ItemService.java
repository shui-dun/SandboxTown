package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.*;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.ItemMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.utils.UUIDNameGenerator;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

    private final EffectService effectService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Lazy
    @Autowired
    private SpriteService spriteService;

    public ItemService(ItemMapper itemMapper, ItemTypeService itemTypeService, EffectService effectService, RedisTemplate<String, Object> redisTemplate) {
        this.itemMapper = itemMapper;
        this.itemTypeService = itemTypeService;
        this.effectService = effectService;
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
    public ItemBo getItemDetailById(String itemId) {
        // 找到物品
        ItemDo item = self.getItemById(itemId);
        if (item == null) {
            return null;
        }
        // 找到物品类型
        ItemTypeBo itemType = itemTypeService.getItemTypeById(item.getItemType());
        // 设置物品类型
        return new ItemBo(item, itemType);
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
    public List<ItemBo> listByOwnerAndPositions(String owner, List<ItemPositionEnum> positions) {
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
    public List<ItemBo> listItemsInEquipmentByOwner(String owner) {
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
    public List<ItemBo> listItemsInItemBarByOwner(String owner) {
        return listByOwnerAndPositions(owner, Arrays.asList(
                ItemPositionEnum.ITEMBAR, ItemPositionEnum.HANDHELD
        ));
    }

    /** 更新或删除物品 */
    @Transactional
    @CacheEvict(value = "item::itemDetail", key = "#item.id")
    public void updateItem(ItemDo item) {
        if (item.getItemCount() <= 0) {
            itemMapper.deleteById(item);
            redisTemplate.delete("item::listByOwner::" + item.getOwner());
        } else {
            itemMapper.updateById(item);
        }
    }

    /** 添加物品 */
    @Transactional
    @Cacheable(value = "item::itemDetail", key = "#item.id")
    public void addItem(ItemDo item) {
        itemMapper.insert(item);
        redisTemplate.delete("item::listByOwner::" + item.getOwner());
    }

    /** 装备物品 */
    @Transactional
    public void equip(String spriteId, String itemId) {
        // 查询该物品详细信息
        ItemBo item = self.getItemDetailById(itemId);
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
            return;
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
        List<ItemBo> equippedItems = listByOwnerAndPositions(spriteId, List.of(itemPosition));
        if (!equippedItems.isEmpty()) {
            ItemDo equippedItem = equippedItems.get(0);
            equippedItem.setPosition(ItemPositionEnum.BACKPACK);
            self.updateItem(equippedItem);
        }
        // 将该物品装备
        item.setPosition(itemPosition);
        self.updateItem(item);
        spriteService.invalidateSpriteCache(spriteId);
    }

    /**
     * 变更物品位置变更
     *
     * @param spriteId       精灵ID
     * @param itemId         物品ID
     * @param targetPosition 目标位置，注意不支持装备位置，装备参见 {@link #equip}
     */
    @Transactional
    public void changeItemPosition(String spriteId, String itemId, ItemPositionEnum targetPosition) {
        // 物品是否存在
        ItemBo itemDetail = self.getItemDetailById(itemId);
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
        List<ItemBo> itemBarItems = listItemsInItemBarByOwner(spriteId);
        // 手持的物品
        ItemBo handHeldItem = listByOwnerAndPositions(spriteId,
                List.of(ItemPositionEnum.HANDHELD)).stream()
                .findFirst().orElse(null);
        // 若目标位置和当前相同，直接返回
        if (originalPosition == targetPosition) {
            return;
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
        spriteService.invalidateSpriteCache(spriteId);
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
                assert item != null;
                // 玩家有该物品，更新数量
                item.setItemCount(item.getItemCount() + count);
                self.updateItem(item);
            }
        }
    }

    @Transactional
    public UseItemResultEnum useItem(String spriteId, String itemId) {
        // 判断物品是否存在
        ItemBo item = getItemDetailById(itemId);
        if (item == null) {
            return UseItemResultEnum.ITEM_NOT_FOUND;
        }
        // 判断角色是否存在
        SpriteBo sprite = spriteService.selectById(spriteId);
        if (sprite == null) {
            return UseItemResultEnum.SPRITE_NOT_FOUND;
        }
        // 判断物品是否属于角色
        if (!item.getOwner().equals(spriteId)) {
            return UseItemResultEnum.NO_PERMISSION;
        }
        return useItem(sprite, item);
    }

    /** 注意该接口没有校验物品是否属于角色，需要谨慎使用 */
    @Transactional
    public UseItemResultEnum useItem(SpriteBo sprite, ItemBo item) {
        // 判断物品是否可用
        Set<ItemLabelEnum> labels = item.getItemTypeObj().getLabels();
        if (!labels.contains(ItemLabelEnum.FOOD) && !labels.contains(ItemLabelEnum.USABLE)) {
            return UseItemResultEnum.ITEM_NOT_USEABLE;
        }
        // 包含FOOD、USABLE以外标签的物品，只能给自己用，不能给别人用
        // 因为对于法棍等既可食用又可手持的物品，如果不加这个判断，就无法用来攻击别人，会变为给别人使用
        if (!item.getOwner().equals(sprite.getId())) {
            if (!labels.stream().allMatch(e -> e == ItemLabelEnum.FOOD || e == ItemLabelEnum.USABLE)) {
                return UseItemResultEnum.ITEM_NOT_USEABLE;
            }
        }
        // 得到物品带来的属性变化
        ItemTypeAttributeDo itemTypeAttribute = item.getItemTypeObj().getAttributes().get(ItemOperationEnum.USE);
        if (itemTypeAttribute != null) {
            // TODO: 根据物品等级计算属性变化
            // 得到角色原先属性
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
            sprite.setVisionRange(sprite.getVisionRange() + itemTypeAttribute.getVisionRangeInc());
            sprite.setAttackRange(sprite.getAttackRange() + itemTypeAttribute.getAttackRangeInc());
            // 判断新属性是否在合理范围内（包含升级操作），随后写入数据库
            spriteService.normalizeAndUpdateSprite(sprite);
            if (spriteAttributeChange.setChanged(sprite)) {
                WSMessageSender.addResponse(WSResponseEnum.SPRITE_ATTRIBUTE_CHANGE, spriteAttributeChange);
            }
        }
        // 向角色施加效果
        // var newEffects = item.getItemTypeObj().getEffects().get(ItemOperationEnum.USE).values();
        // 为避免空指针异常，改为：
        var newEffects = item.getItemTypeObj().getEffects().getOrDefault(ItemOperationEnum.USE, new HashMap<>()).values();
        for (ItemTypeEffectDo effect : newEffects) {
            effectService.addEffect(sprite.getId(), effect.getEffect(), effect.getDuration());
        }

        // 物品数目减1
        item.setItemCount(item.getItemCount() - 1);
        self.updateItem(item);

        spriteService.invalidateSpriteCache(sprite.getId());
        spriteService.invalidateSpriteCache(item.getOwner());
        WSMessageSender.addResponse(WSResponseEnum.ITEM_GAIN, new ItemGainVo(item.getOwner(), item.getItemType(), -1));
        return UseItemResultEnum.ITEM_USE_SUCCESS;
    }

    /** 判断某个物品是否是某类型 */
    public boolean itemTypeIs(String itemId, ItemTypeEnum itemType) {
        return itemId.startsWith(itemType + "_");
    }
}
