package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.*;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.*;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.NameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.shuidun.sandbox_town_backend.mixin.Constants.EXP_PER_LEVEL;

@Slf4j
@Service
public class SpriteService {
    private final SpriteMapper spriteMapper;

    private final SpriteTypeMapper spriteTypeMapper;

    private final ItemService itemService;

    private final ItemTypeAttributeMapper itemTypeAttributeMapper;

    private final ItemTypeLabelMapper itemTypeLabelMapper;

    private final ItemMapper itemMapper;

    private final SpriteEffectMapper spriteEffectMapper;

    private final EffectMapper effectMapper;


    @Value("${mapId}")
    private String mapId;

    public SpriteService(SpriteMapper spriteMapper, SpriteTypeMapper spriteTypeMapper, ItemService itemService, ItemTypeAttributeMapper itemTypeAttributeMapper, ItemTypeLabelMapper itemTypeLabelMapper, ItemMapper itemMapper, SpriteEffectMapper spriteEffectMapper, EffectMapper effectMapper) {
        this.spriteMapper = spriteMapper;
        this.spriteTypeMapper = spriteTypeMapper;
        this.itemService = itemService;
        this.itemTypeAttributeMapper = itemTypeAttributeMapper;
        this.itemTypeLabelMapper = itemTypeLabelMapper;
        this.itemMapper = itemMapper;
        this.spriteEffectMapper = spriteEffectMapper;
        this.effectMapper = effectMapper;
    }

    /** 将cache中的信息赋值给sprite */
    private void assignCacheToSprite(SpriteDo sprite) {
        var spriteCache = GameCache.spriteCacheMap.get(sprite.getId());
        if (spriteCache != null) {
            sprite.setX((int) Math.round(spriteCache.getX()));
            sprite.setY((int) Math.round(spriteCache.getY()));
            sprite.setVx(spriteCache.getVx());
            sprite.setVy(spriteCache.getVy());
            sprite.setStatus(spriteCache.getStatus());
        }
    }

    /**
     * 获得精灵的属性增量信息
     */
    private void assignIncToSprite(SpriteDo sprite) {
        // 首先将属性增强全都设置为0
        sprite.setHungerInc(0);
        sprite.setHpInc(0);
        sprite.setAttackInc(0);
        sprite.setDefenseInc(0);
        sprite.setSpeedInc(0);
        // 对于所有装备，计算属性增量
        for (ItemDo item : sprite.getEquipments()) {
            // 判断物品的位置
            ItemPositionEnum position = item.getPosition();
            // 增益信息
            ItemTypeAttributeDo attributesInc;
            // 如果是手持
            if (position == ItemPositionEnum.HANDHELD) {
                attributesInc = item.getItemTypeObj().getAttributes().get(ItemOperationEnum.HANDHELD);
            } else {
                attributesInc = item.getItemTypeObj().getAttributes().get(ItemOperationEnum.EQUIP);
            }
            if (attributesInc == null) {
                continue;
            }
            sprite.setHungerInc(sprite.getHungerInc() + attributesInc.getHungerInc());
            sprite.setHpInc(sprite.getHpInc() + attributesInc.getHpInc());
            sprite.setAttackInc(sprite.getAttackInc() + attributesInc.getAttackInc());
            sprite.setDefenseInc(sprite.getDefenseInc() + attributesInc.getDefenseInc());
            sprite.setSpeedInc(sprite.getSpeedInc() + attributesInc.getSpeedInc());
        }
    }

    /**
     * 为精灵设置效果列表
     */
    private void assignEffectToSprite(SpriteDo sprite) {
        sprite.setEffects(new ArrayList<>());
        // 从数据库中获取精灵的效果列表 （但注意这不包含装备的效果）
        Map<EffectEnum, SpriteEffectDo> spriteEffectMap = spriteEffectMapper.selectBySprite(sprite.getId()).stream().collect(Collectors.toMap(SpriteEffectDo::getEffect, Function.identity()));
        // 获得装备的效果列表
        List<ItemTypeEffectDo> equipmentEffectList = new ArrayList<>();
        for (ItemDo item : sprite.getEquipments()) {
            // 判断物品的位置
            ItemPositionEnum position = item.getPosition();
            // 如果是手持
            if (position == ItemPositionEnum.HANDHELD) {
                // equipmentEffectList.addAll(item.getItemTypeObj().getEffects().get(ItemOperationEnum.HANDHELD).values());
                // 使用Optional来避免空指针异常
                // map()函数会对存在的值进行计算，返回一个新的Optional。如果源Optional为空，它将直接返回一个空的Optional
                // ifPresent()函数在Optional值存在时会执行给定的lambda表达式
                Optional.ofNullable(item.getItemTypeObj().getEffects())
                        .map(e -> e.get(ItemOperationEnum.HANDHELD))
                        .ifPresent(v -> equipmentEffectList.addAll(v.values()));
            } else { // 如果是装备栏
                // equipmentEffectList.addAll(item.getItemTypeObj().getEffects().get(ItemOperationEnum.EQUIP).values());
                Optional.ofNullable(item.getItemTypeObj().getEffects())
                        .map(e -> e.get(ItemOperationEnum.EQUIP))
                        .ifPresent(v -> equipmentEffectList.addAll(v.values()));
            }
        }
        // 将装备的效果列表和精灵的效果列表合并
        for (ItemTypeEffectDo equipmentEffect : equipmentEffectList) {
            // 如果精灵的效果列表中没有这个效果
            if (!spriteEffectMap.containsKey(equipmentEffect.getEffect())) {
                // 直接加入
                SpriteEffectDo spriteEffectDo = new SpriteEffectDo();
                spriteEffectDo.setSprite(sprite.getId());
                spriteEffectDo.setEffect(equipmentEffect.getEffect());
                // 装备的效果时效显然是永久
                spriteEffectDo.setDuration(-1);
                spriteEffectDo.setExpire(-1L);
                spriteEffectMap.put(equipmentEffect.getEffect(), spriteEffectDo);
            } else { // 如果精灵的效果列表中有这个效果
                SpriteEffectDo spriteEffectDo = spriteEffectMap.get(equipmentEffect.getEffect());
                // 装备的效果时效显然是永久
                spriteEffectDo.setDuration(-1);
                spriteEffectDo.setExpire(-1L);
            }
        }
        // 添加效果详细信息到spriteEffectMap
        if (spriteEffectMap.size() > 0) {
            List<EffectDo> effectList = effectMapper.selectBatchIds(spriteEffectMap.keySet());
            // 按照效果名组织效果列表
            Map<EffectEnum, EffectDo> effectMap = effectList.stream().collect(Collectors.toMap(EffectDo::getId, Function.identity()));
            // 将效果详细信息添加到spriteEffectMap
            for (SpriteEffectDo spriteEffectDo : spriteEffectMap.values()) {
                spriteEffectDo.setEffectObj(effectMap.get(spriteEffectDo.getEffect()));
            }
        }
        sprite.setEffects(new ArrayList<>(spriteEffectMap.values()));
    }

    /** 为精灵设置装备、属性增量信息和效果列表 */
    private void assignEquipmentsAndAttributeIncAndEffectToSprite(SpriteDo sprite) {
        // 获取装备列表
        var detailedItems = itemService.listItemsInEquipmentByOwnerWithDetail(sprite.getId());
        // 设置装备列表
        sprite.setEquipments(detailedItems);
        // 设置属性增量信息
        assignIncToSprite(sprite);
        // 设置效果列表
        assignEffectToSprite(sprite);
    }

    /** 根据id获取角色信息（只带有缓存信息） */
    public SpriteDo selectById(String id) {
        SpriteDo sprite = spriteMapper.selectById(id);
        if (sprite == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        // 看看有没有cached信息
        assignCacheToSprite(sprite);
        return sprite;
    }

    /** 根据id获取角色详细信息（带有缓存信息、类型信息、装备信息、属性增量信息、效果列表信息），用于向前端展示 */
    public SpriteDo selectByIdWithTypeAndEquipmentsAndIncAndEffect(String id) {
        // 获得带有类型信息的sprite
        SpriteDo sprite = spriteMapper.selectByIdWithType(id);
        if (sprite == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        // 看看有没有cached信息
        assignCacheToSprite(sprite);
        // 获取装备信息、属性增量信息、效果列表
        assignEquipmentsAndAttributeIncAndEffectToSprite(sprite);
        return sprite;
    }

    /** 根据id获取角色信息（带有缓存信息、装备信息、属性增量信息、效果列表信息），用于后端计算 */
    public SpriteDo selectByIdWithEquipmentsAndIncAndEffect(String id) {
        SpriteDo sprite = spriteMapper.selectById(id);
        if (sprite == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        // 看看有没有cached信息
        assignCacheToSprite(sprite);
        // 获取装备信息、属性增量信息、效果列表
        assignEquipmentsAndAttributeIncAndEffectToSprite(sprite);
        return sprite;
    }

    /** 判断角色属性值是否在合理范围内（包含升级操作） */
    @Transactional
    public SpriteDo normalizeAndUpdateSprite(SpriteDo sprite) {
        // 如果经验值足够升级，则升级
        if (sprite.getExp() >= EXP_PER_LEVEL) {
            sprite.setLevel(sprite.getLevel() + 1);
            sprite.setExp(sprite.getExp() - EXP_PER_LEVEL);
            // 更新玩家属性
            sprite.setMoney(sprite.getMoney() + 15);
            sprite.setHunger(sprite.getHunger() + 10);
            sprite.setHp(sprite.getHp() + 10);
            sprite.setAttack(sprite.getAttack() + 2);
            sprite.setDefense(sprite.getDefense() + 2);
            sprite.setSpeed(sprite.getSpeed() + 2);
        }
        // 判断属性是否在合理范围内
        if (sprite.getHunger() > 100) {
            sprite.setHunger(100);
        }
        if (sprite.getHunger() < 0) {
            sprite.setHunger(0);
        }
        if (sprite.getHp() > 100) {
            sprite.setHp(100);
        }
        if (sprite.getHp() < 0) {
            // 不能设置为0，因为0代表死亡
            sprite.setHp(1);
        }
        if (sprite.getAttack() < 0) {
            sprite.setAttack(0);
        }
        if (sprite.getDefense() < 0) {
            sprite.setDefense(0);
        }
        if (sprite.getSpeed() < 0) {
            sprite.setSpeed(0);
        }
        spriteMapper.updateById(sprite);
        return sprite;
    }

    /** 得到某个地图上的所有角色 */
    public List<SpriteDo> getSpritesByMap(String map) {
        return spriteMapper.selectByMapId(map);
    }

    /** 生成随机的指定类型的角色，并写入数据库 */
    public SpriteDo generateRandomSprite(SpriteTypeEnum type, String owner, int x, int y) {
        SpriteDo sprite = new SpriteDo();
        SpriteTypeDo spriteType = spriteTypeMapper.selectById(type);
        sprite.setId(NameGenerator.generateItemName(type.name()));
        sprite.setType(type);
        sprite.setOwner(owner);
        // 根据基础属性值和随机数随机生成角色的属性
        double scale = 0.8 + Math.random() * 0.4;
        sprite.setMoney((int) (spriteType.getBasicMoney() * scale));
        scale = 0.8 + Math.random() * 0.4;
        sprite.setExp((int) (spriteType.getBasicExp() * scale));
        scale = 0.8 + Math.random() * 0.4;
        sprite.setLevel((int) (spriteType.getBasicLevel() * scale));
        if (sprite.getLevel() < 1) {
            sprite.setLevel(1);
        }
        scale = 0.8 + Math.random() * 0.4;
        sprite.setHunger((int) (spriteType.getBasicHunger() * scale));
        if (sprite.getHunger() > 100) {
            sprite.setHunger(100);
        }
        scale = 0.8 + Math.random() * 0.4;
        sprite.setHp((int) (spriteType.getBasicHp() * scale));
        scale = 0.8 + Math.random() * 0.4;
        sprite.setAttack((int) (spriteType.getBasicAttack() * scale));
        scale = 0.8 + Math.random() * 0.4;
        sprite.setDefense((int) (spriteType.getBasicDefense() * scale));
        scale = 0.8 + Math.random() * 0.4;
        sprite.setSpeed((int) (spriteType.getBasicSpeed() * scale));
        sprite.setX(x);
        sprite.setY(y);
        // 宽度和高度使用相同的scale
        scale = 0.8 + Math.random() * 0.4;
        sprite.setWidth((int) (spriteType.getBasicWidth() * scale));
        sprite.setHeight((int) (spriteType.getBasicHeight() * scale));
        sprite.setMap(mapId);
        spriteMapper.insert(sprite);
        return sprite;
    }

    public List<SpriteDo> getOnlineSprites() {
        if (GameCache.spriteCacheMap.isEmpty()) {
            return new ArrayList<>();
        }
        List<SpriteDo> sprites = spriteMapper.selectBatchIds(GameCache.spriteCacheMap.keySet());
        // 更新坐标为缓存中的最新坐标
        for (SpriteDo sprite : sprites) {
            sprite.setX((int) Math.round(GameCache.spriteCacheMap.get(sprite.getId()).getX()));
            sprite.setY((int) Math.round(GameCache.spriteCacheMap.get(sprite.getId()).getY()));
        }
        return sprites;
    }

    public MyAndMyPetInfoVo getMyAndMyPetInfo(String ownerId) {
        MyAndMyPetInfoVo myAndMyPetInfo = new MyAndMyPetInfoVo();
        myAndMyPetInfo.setMe(spriteMapper.selectById(ownerId));
        myAndMyPetInfo.setMyPets(spriteMapper.selectByOwner(ownerId));
        return myAndMyPetInfo;
    }

    /** 得到玩家的所有宠物 */
    public List<SpriteDo> selectByOwner(String ownerId) {
        return spriteMapper.selectByOwner(ownerId);
    }

    /** 得到所有未被玩家拥有的角色 */
    public List<SpriteDo> getUnownedSprites() {
        return spriteMapper.selectUnownedSprites();
    }

    /**
     * 使用物品
     */
    @Transactional
    public List<WSResponseVo> useItem(String owner, String itemId) {
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
        sprite = normalizeAndUpdateSprite(sprite);
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
        List<ItemDo> itemInItemBar = itemService.listItemsInItemBarByOwner(spriteId);
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

        responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, itemService.listItemsInItemBarByOwner(spriteId)));
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
        List<ItemDo> itemInItemBar = itemService.listItemsInItemBarByOwner(spriteId);
        if (itemInItemBar.size() >= Constants.ITEM_BAR_SIZE && item.getPosition() != ItemPositionEnum.HANDHELD) {
            throw new BusinessException(StatusCodeEnum.ITEMBAR_FULL);
        }
        // 将该物品放入物品栏
        item.setPosition(ItemPositionEnum.ITEMBAR);
        itemMapper.updateById(item);

        responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, itemService.listItemsInItemBarByOwner(spriteId)));
        return responses;
    }

    /** 装备物品 */
    @Transactional
    public Iterable<WSResponseVo> equip(String spriteId, String itemId) {
        List<WSResponseVo> responses = new ArrayList<>();
        // 查询该物品详细信息
        ItemDo item = itemService.getItemDetailById(itemId);
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
        List<ItemDo> equippedItems = itemMapper.selectByOwnerAndPosition(spriteId, itemPosition);
        if (equippedItems != null && !equippedItems.isEmpty()) {
            ItemDo equippedItem = equippedItems.get(0);
            equippedItem.setPosition(ItemPositionEnum.BACKPACK);
            itemMapper.updateById(equippedItem);
        }
        // 将该物品装备
        item.setPosition(itemPosition);
        itemMapper.updateById(item);

        // 如果原先在物品栏，发射物品栏通知
        if (originalPosition == ItemPositionEnum.ITEMBAR || originalPosition == ItemPositionEnum.HANDHELD) {
            responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, itemService.listItemsInItemBarByOwner(spriteId)));
        }

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
            responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, itemService.listItemsInItemBarByOwner(spriteId)));
        }
        return responses;
    }
}
