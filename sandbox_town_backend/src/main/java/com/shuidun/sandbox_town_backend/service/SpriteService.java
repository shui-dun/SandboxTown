package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.*;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.*;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.NameGenerator;
import com.shuidun.sandbox_town_backend.websocket.WSRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
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

    private final ItemMapper itemMapper;

    private final SpriteEffectMapper spriteEffectMapper;

    private final EffectMapper effectMapper;


    @Value("${mapId}")
    private String mapId;

    public SpriteService(SpriteMapper spriteMapper, SpriteTypeMapper spriteTypeMapper, ItemService itemService, ItemMapper itemMapper, SpriteEffectMapper spriteEffectMapper, EffectMapper effectMapper) {
        this.spriteMapper = spriteMapper;
        this.spriteTypeMapper = spriteTypeMapper;
        this.itemService = itemService;
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
        sprite.setVisionRangeInc(0);
        sprite.setAttackRangeInc(0);
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
            sprite.setVisionRangeInc(sprite.getVisionRangeInc() + attributesInc.getVisionRangeInc());
            sprite.setAttackRangeInc(sprite.getAttackRangeInc() + attributesInc.getAttackRangeInc());
        }
    }

    /**
     * 为精灵设置效果列表
     */
    private void assignEffectToSprite(SpriteDo sprite) {
        sprite.setEffects(new ArrayList<>());
        // 从数据库中获取精灵的效果列表 （但注意这不包含装备的效果）
        Map<EffectEnum, SpriteEffectDo> spriteEffectMap = selectEffectsAndDeleteExpiredEffects(sprite.getId()).stream().collect(Collectors.toMap(SpriteEffectDo::getEffect, Function.identity()));
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
            return null;
        }
        // 看看有没有cached信息
        assignCacheToSprite(sprite);
        return sprite;
    }

    /** 根据id获取角色信息（只带有缓存信息和类型信息） */
    public SpriteDo selectByIdWithType(String id) {
        SpriteDo sprite = spriteMapper.selectByIdWithType(id);
        if (sprite == null) {
            return null;
        }
        // 看看有没有cached信息
        assignCacheToSprite(sprite);
        return sprite;
    }

    /** 根据id获取角色详细信息（带有缓存信息、类型信息、装备信息、属性增量信息、效果列表信息） */
    public SpriteDo selectByIdWithDetail(String id) {
        // 获得带有类型信息的sprite
        SpriteDo sprite = spriteMapper.selectByIdWithType(id);
        if (sprite == null) {
            return null;
        }
        // 看看有没有cached信息
        assignCacheToSprite(sprite);
        // 获取装备信息、属性增量信息、效果列表
        assignEquipmentsAndAttributeIncAndEffectToSprite(sprite);
        return sprite;
    }

    /** 判断角色属性值是否在合理范围内（包含升级操作） */
    @Transactional
    public Pair<SpriteDo, List<WSResponseVo>> normalizeAndUpdateSprite(SpriteDo sprite) {
        List<WSResponseVo> responseList = new ArrayList<>();
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
            sprite.setVisionRange(sprite.getVisionRange() + 50);
            sprite.setAttackRange(sprite.getAttackRange() + 1);
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
            sprite.setHp(0);
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
        if (sprite.getVisionRange() < 0) {
            sprite.setVisionRange(0);
        }
        if (sprite.getAttackRange() < 0) {
            sprite.setAttackRange(0);
        }
        // 判断是否死亡
        if (sprite.getHp() == 0) {
            // 如果是玩家，则扣除金钱和清除经验，恢复饱腹值和生命值，并设置坐标为原点
            if (sprite.getType() == SpriteTypeEnum.USER) {
                sprite.setMoney(Math.max(0, sprite.getMoney() - 50));
                sprite.setExp(0);
                sprite.setHunger(100);
                sprite.setHp(100);
                sprite.setX(0);
                sprite.setY(0);
                spriteMapper.updateById(sprite);
                GameCache.spriteCacheMap.get(sprite.getId()).setX(0);
                GameCache.spriteCacheMap.get(sprite.getId()).setY(0);
                responseList.add(new WSResponseVo(WSResponseEnum.COORDINATE, new CoordinateVo(
                        sprite.getId(),
                        sprite.getX(),
                        sprite.getY(),
                        0, 0
                )));
            } else { // 否则，删除
                spriteMapper.deleteById(sprite.getId());
                responseList.add(offline(sprite.getId()));
            }
        } else {
            spriteMapper.updateById(sprite);
        }
        return Pair.of(sprite, responseList);
    }

    /** 得到某个地图上的所有角色 */
    public List<SpriteDo> getSpritesByMap(String map) {
        var list = spriteMapper.selectByMapId(map);
        // 得到缓存信息
        for (SpriteDo sprite : list) {
            assignCacheToSprite(sprite);
        }
        return list;
    }

    /** 生成固定的（即各属性值严格等于其精灵类型的基础属性值）指定类型的角色，并写入数据库 */
    public SpriteDo generateFixedSprite(SpriteTypeEnum type, String id, String owner, int x, int y) {
        SpriteDo sprite = new SpriteDo();
        SpriteTypeDo spriteType = spriteTypeMapper.selectById(type);
        sprite.setId(id);
        sprite.setType(type);
        sprite.setOwner(owner);
        sprite.setMoney(spriteType.getBasicMoney());
        sprite.setExp(spriteType.getBasicExp());
        sprite.setLevel(spriteType.getBasicLevel());
        sprite.setHunger(spriteType.getBasicHunger());
        sprite.setHp(spriteType.getBasicHp());
        sprite.setAttack(spriteType.getBasicAttack());
        sprite.setDefense(spriteType.getBasicDefense());
        sprite.setSpeed(spriteType.getBasicSpeed());
        sprite.setVisionRange(spriteType.getBasicVisionRange());
        sprite.setAttackRange(spriteType.getBasicAttackRange());
        sprite.setX(x);
        sprite.setY(y);
        sprite.setWidth(spriteType.getBasicWidth());
        sprite.setHeight(spriteType.getBasicHeight());
        sprite.setMap(mapId);
        spriteMapper.insert(sprite);
        return sprite;
    }

    /** 生成随机的指定类型的角色，并写入数据库 */
    public SpriteDo generateRandomSprite(SpriteTypeEnum type, String id, String owner, int x, int y) {
        SpriteDo sprite = new SpriteDo();
        SpriteTypeDo spriteType = spriteTypeMapper.selectById(type);
        sprite.setId(id);
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
        scale = 0.8 + Math.random() * 0.4;
        sprite.setVisionRange((int) (spriteType.getBasicVisionRange() * scale));
        scale = 0.8 + Math.random() * 0.4;
        sprite.setAttackRange((int) (spriteType.getBasicAttackRange() * scale));
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

    public SpriteDo generateRandomSprite(SpriteTypeEnum type, String owner, int x, int y) {
        String id = NameGenerator.generateItemName(type.name());
        return generateRandomSprite(type, id, owner, x, y);
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

    /** 删除精灵过期的效果并返回所有未过期的效果 */
    public List<SpriteEffectDo> selectEffectsAndDeleteExpiredEffects(String spriteId) {
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
     * 使用物品
     */
    @Transactional
    public List<WSResponseVo> useItem(String owner, String itemId) {
        List<WSResponseVo> responseList = new ArrayList<>();
        // 判断物品是否存在
        ItemDo item = itemService.getItemDetailById(itemId);
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断角色是否拥有该物品
        if (!item.getOwner().equals(owner)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        // 判断物品是否可用
        Set<ItemLabelEnum> labels = item.getItemTypeObj().getLabels();
        if (!labels.contains(ItemLabelEnum.FOOD) && !labels.contains(ItemLabelEnum.USABLE)) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_USABLE);
        }
        // 得到物品带来的属性变化
        ItemTypeAttributeDo itemTypeAttribute = item.getItemTypeObj().getAttributes().get(ItemOperationEnum.USE);
        if (itemTypeAttribute != null) {
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
            sprite.setVisionRange(sprite.getVisionRange() + itemTypeAttribute.getVisionRangeInc());
            sprite.setAttackRange(sprite.getAttackRange() + itemTypeAttribute.getAttackRangeInc());
            // 判断新属性是否在合理范围内（包含升级操作），随后写入数据库
            sprite = normalizeAndUpdateSprite(sprite).getFirst();
            if (spriteAttributeChange.setChanged(sprite)) {
                responseList.add(new WSResponseVo(WSResponseEnum.SPRITE_ATTRIBUTE_CHANGE, spriteAttributeChange));
            }
        }
        // 查询精灵原有效果
        List<SpriteEffectDo> effects = selectEffectsAndDeleteExpiredEffects(owner);
        Map<EffectEnum, SpriteEffectDo> effectMap = effects.stream().collect(Collectors.toMap(SpriteEffectDo::getEffect, effect -> effect));
        // 向角色施加效果
        // var newEffects = item.getItemTypeObj().getEffects().get(ItemOperationEnum.USE).values();
        // 为避免空指针异常，改为：
        var newEffects = item.getItemTypeObj().getEffects().getOrDefault(ItemOperationEnum.USE, new HashMap<>()).values();
        for (ItemTypeEffectDo effect : newEffects) {
            // 判断是否已经有该效果
            if (effectMap.containsKey(effect.getEffect())) {
                // 更新效果
                SpriteEffectDo spriteEffect = effectMap.get(effect.getEffect());
                // 如果效果是永久的
                if (effect.getDuration() == -1 || spriteEffect.getDuration() == -1) {
                    spriteEffect.setDuration(-1);
                    spriteEffect.setExpire(-1L);
                } else {
                    spriteEffect.setDuration(spriteEffect.getDuration() + effect.getDuration());
                    spriteEffect.setExpire(spriteEffect.getExpire() + effect.getDuration() * 1000);
                }
                spriteEffectMapper.update(spriteEffect);
            } else {
                // 添加效果
                SpriteEffectDo spriteEffect = new SpriteEffectDo();
                spriteEffect.setSprite(owner);
                spriteEffect.setEffect(effect.getEffect());
                spriteEffect.setDuration(effect.getDuration());
                spriteEffect.setExpire(effect.getDuration() == -1 ? -1L : System.currentTimeMillis() + effect.getDuration() * 1000);
                spriteEffectMapper.insert(spriteEffect);
            }
        }

        // 判断是否是最后一个物品
        if (item.getItemCount() <= 1) {
            // 删除物品
            itemMapper.deleteById(item);
        } else {
            // 更新物品数量
            item.setItemCount(item.getItemCount() - 1);
            itemMapper.updateById(item);
        }

        // 可能有精灵效果变化
        responseList.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, null));

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
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, null));

        return responses;
    }

    /**
     * 放入物品栏
     */
    @Transactional
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
        List<ItemDo> itemInItemBar = itemService.listItemsInItemBarByOwner(spriteId);
        if (itemInItemBar.size() >= Constants.ITEM_BAR_SIZE && item.getPosition() != ItemPositionEnum.HANDHELD) {
            throw new BusinessException(StatusCodeEnum.ITEMBAR_FULL);
        }
        // 将该物品放入物品栏
        item.setPosition(ItemPositionEnum.ITEMBAR);
        itemMapper.updateById(item);

        responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, itemService.listItemsInItemBarByOwner(spriteId)));
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, null));

        return responses;
    }

    /** 装备物品 */
    @Transactional
    public List<WSResponseVo> equip(String spriteId, String itemId) {
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
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, null));

        return responses;
    }

    /** 放入背包 */
    @Transactional
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
            responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, itemService.listItemsInItemBarByOwner(spriteId)));
        }
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, null));

        return responses;
    }

    /**
     * 减少精灵饱腹值
     *
     * @param spriteIds 精灵id集合
     * @param val       减少值
     */
    public void reduceSpritesHunger(Collection<String> spriteIds, int val) {
        if (spriteIds == null || spriteIds.isEmpty()) {
            return;
        }
        spriteMapper.reduceSpritesHunger(spriteIds, val);
    }

    /**
     * 恢复精灵生命
     *
     * @param spriteIds 精灵id集合
     * @param minHunger 最小饱腹值，即饱腹值低于该值时不恢复生命
     * @param incVal    恢复值
     */
    public void recoverSpritesLife(Collection<String> spriteIds, int minHunger, int incVal) {
        if (spriteIds == null || spriteIds.isEmpty()) {
            return;
        }
        spriteMapper.recoverSpritesLife(spriteIds, minHunger, incVal);
    }

    @Transactional
    public List<WSResponseVo> attack(SpriteDo sourceSprite, SpriteDo targetSprite) {
        List<WSResponseVo> responses = new ArrayList<>();
        HpChangeVo hpChangeVo = new HpChangeVo();
        hpChangeVo.setId(targetSprite.getId());
        hpChangeVo.setOriginHp(targetSprite.getHp());
        // 计算伤害
        int damage = sourceSprite.getAttack() + sourceSprite.getAttackInc() -
                (targetSprite.getDefense() + targetSprite.getDefenseInc());
        if (damage <= 0) {
            damage = 0;
        }
        // 扣除目标精灵生命
        targetSprite.setHp(targetSprite.getHp() - damage);
        // 判断目标精灵是否死亡
        if (targetSprite.getHp() <= 0) {
            targetSprite.setHp(0);
        }
        hpChangeVo.setHpChange(targetSprite.getHp() - hpChangeVo.getOriginHp());
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_HP_CHANGE, hpChangeVo));
        // 更新目标精灵
        responses.addAll(normalizeAndUpdateSprite(targetSprite).getSecond());
        return responses;
    }

    public WSResponseVo offline(String spriteId) {
        List<String> ids = new ArrayList<>();
        ids.add(spriteId);
        // 读取精灵的所有宠物
        List<SpriteDo> pets = selectByOwner(spriteId);
        pets.forEach(pet -> ids.add(pet.getId()));
        // 删除精灵以及其宠物坐标等信息
        GameCache.spriteCacheMap.remove(spriteId);
        pets.forEach(pet -> GameCache.spriteCacheMap.remove(pet.getId()));
        return new WSResponseVo(WSResponseEnum.OFFLINE, new OfflineVo(ids));

    }
}
