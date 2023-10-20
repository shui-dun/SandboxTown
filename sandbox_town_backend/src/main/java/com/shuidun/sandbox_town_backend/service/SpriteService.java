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
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SpriteService {
    private final SpriteMapper spriteMapper;

    private final SpriteTypeMapper spriteTypeMapper;

    private final ItemService itemService;

    private final ItemMapper itemMapper;

    private final SpriteEffectMapper spriteEffectMapper;

    private final EffectMapper effectMapper;

    private final BuildingMapper buildingMapper;

    private final SpriteRefreshMapper spriteRefreshMapper;

    private final FeedMapper feedMapper;

    private final VictoryAttributeRewardMapper victoryAttributeRewardMapper;

    private final VictoryItemRewardMapper victoryItemRewardMapper;


    @Value("${mapId}")
    private String mapId;

    public SpriteService(SpriteMapper spriteMapper, SpriteTypeMapper spriteTypeMapper, ItemService itemService, ItemMapper itemMapper, SpriteEffectMapper spriteEffectMapper, EffectMapper effectMapper, BuildingMapper buildingMapper, SpriteRefreshMapper spriteRefreshMapper, FeedMapper feedMapper, VictoryAttributeRewardMapper victoryAttributeRewardMapper, VictoryItemRewardMapper victoryItemRewardMapper) {
        this.spriteMapper = spriteMapper;
        this.spriteTypeMapper = spriteTypeMapper;
        this.itemService = itemService;
        this.itemMapper = itemMapper;
        this.spriteEffectMapper = spriteEffectMapper;
        this.effectMapper = effectMapper;
        this.buildingMapper = buildingMapper;
        this.spriteRefreshMapper = spriteRefreshMapper;
        this.feedMapper = feedMapper;
        this.victoryAttributeRewardMapper = victoryAttributeRewardMapper;
        this.victoryItemRewardMapper = victoryItemRewardMapper;
    }

    /** 将cache中的信息赋值给sprite */
    private void assignCacheToSprite(SpriteDo sprite) {
        var spriteCache = GameCache.spriteCacheMap.get(sprite.getId());
        if (spriteCache != null) {
            sprite.setX(spriteCache.getX());
            sprite.setY(spriteCache.getY());
            sprite.setCache(spriteCache);
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
        if (sprite.getExp() >= Constants.EXP_PER_LEVEL) {
            // 得到类型信息
            SpriteTypeDo spriteType = spriteTypeMapper.selectById(sprite.getType());
            // 升级级数
            int levelUp = sprite.getExp() / Constants.EXP_PER_LEVEL;
            // 升级
            sprite.setLevel(sprite.getLevel() + levelUp);
            // 减少经验值
            sprite.setExp(sprite.getExp() - levelUp * Constants.EXP_PER_LEVEL);
            // 更新玩家属性
            sprite.setMoney(sprite.getMoney() + levelUp * Constants.MONEY_GAIN_ON_LEVEL_UP);
            sprite.setHunger(Constants.MAX_HUNGER);
            sprite.setHp(Constants.MAX_HP);
            // 每次升级，增加基础属性值的1/4，至少增加1
            sprite.setAttack(sprite.getAttack() + Math.max(1, spriteType.getBasicAttack() / 4) * levelUp);
            sprite.setDefense(sprite.getDefense() + Math.max(1, spriteType.getBasicDefense() / 4) * levelUp);
            sprite.setSpeed(sprite.getSpeed() + Math.max(1, spriteType.getBasicSpeed() / 4) * levelUp);
            sprite.setVisionRange(sprite.getVisionRange() + Math.max(1, spriteType.getBasicVisionRange() / 4) * levelUp);
            sprite.setAttackRange(sprite.getAttackRange() + Math.max(1, spriteType.getBasicAttackRange() / 4) * levelUp);
        }
        // 判断属性是否在合理范围内
        if (sprite.getHunger() > Constants.MAX_HUNGER) {
            sprite.setHunger(Constants.MAX_HUNGER);
        }
        if (sprite.getHunger() < 0) {
            sprite.setHunger(0);
        }
        if (sprite.getHp() > Constants.MAX_HP) {
            sprite.setHp(Constants.MAX_HP);
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
                sprite.setMoney(Math.max(0, sprite.getMoney() - Constants.MONEY_LOST_ON_DEATH));
                sprite.setExp(0);
                sprite.setHunger(Constants.MAX_HUNGER);
                sprite.setHp(Constants.MAX_HP);
                sprite.setX(0.0);
                sprite.setY(0.0);
                spriteMapper.updateById(sprite);
                var spriteCache = GameCache.spriteCacheMap.get(sprite.getId());
                spriteCache.setX(0);
                spriteCache.setY(0);
                // 修复玩家死亡之后有可能位置不变，没有回到出生点的bug
                spriteCache.setLastMoveTime(System.currentTimeMillis() + 500);
                responseList.add(new WSResponseVo(WSResponseEnum.COORDINATE, new CoordinateVo(
                        sprite.getId(),
                        sprite.getX(),
                        sprite.getY(),
                        0, 0
                )));
            } else { // 否则，删除
                // 修改精灵的所有建筑的主人设置为null
                buildingMapper.updateOwnerByOwner(sprite.getId(), null);
                // 使精灵下线（同时递归下线它的宠物）
                responseList.add(offline(sprite.getId()));
                // 删除精灵（同时递归删除它的宠物）
                spriteMapper.deleteById(sprite.getId());
            }
        } else {
            // 如果精灵存在，则更新精灵，否则添加精灵
            spriteMapper.insertOrUpdateById(sprite);
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
    @Transactional
    public SpriteDo generateFixedSprite(SpriteTypeEnum type, String id, String owner, double x, double y) {
        SpriteDo sprite = new SpriteDo();
        SpriteTypeDo spriteType = spriteTypeMapper.selectById(type);
        sprite.setId(id);
        sprite.setType(type);
        sprite.setOwner(owner);
        sprite.setMoney(spriteType.getBasicMoney());
        sprite.setExp(spriteType.getBasicExp());
        sprite.setLevel(spriteType.getBasicLevel());
        // 将等级降为1，全部赋给经验值
        if (sprite.getLevel() > 1) {
            sprite.setExp(sprite.getExp() + (sprite.getLevel() - 1) * Constants.EXP_PER_LEVEL);
            sprite.setLevel(1);
        }
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
        normalizeAndUpdateSprite(sprite);
        return sprite;
    }

    /** 生成随机的指定类型的角色，并写入数据库 */
    @Transactional
    public SpriteDo generateRandomSprite(SpriteTypeEnum type, String id, String owner, double x, double y) {
        SpriteDo sprite = new SpriteDo();
        SpriteTypeDo spriteType = spriteTypeMapper.selectById(type);
        sprite.setId(id);
        sprite.setType(type);
        sprite.setOwner(owner);
        // 根据基础属性值和随机数随机生成角色的属性
        double scale = 0.8 + GameCache.random.nextDouble() * 0.4;
        sprite.setMoney((int) (spriteType.getBasicMoney() * scale));
        scale = -5 + GameCache.random.nextInt(11);
        sprite.setLevel((int) (spriteType.getBasicLevel() + scale));
        if (sprite.getLevel() < 1) {
            sprite.setLevel(1);
        }
        scale = 0.8 + GameCache.random.nextDouble() * 0.4;
        sprite.setExp((int) (spriteType.getBasicExp() * scale));
        // 将等级降为1，全部赋给经验值
        if (sprite.getLevel() > 1) {
            sprite.setExp(sprite.getExp() + (sprite.getLevel() - 1) * Constants.EXP_PER_LEVEL);
            sprite.setLevel(1);
        }
        scale = 0.8 + GameCache.random.nextDouble() * 0.4;
        sprite.setHunger((int) (spriteType.getBasicHunger() * scale));
        scale = 0.8 + GameCache.random.nextDouble() * 0.4;
        sprite.setHp((int) (spriteType.getBasicHp() * scale));
        scale = 0.8 + GameCache.random.nextDouble() * 0.4;
        sprite.setAttack((int) (spriteType.getBasicAttack() * scale));
        scale = 0.8 + GameCache.random.nextDouble() * 0.4;
        sprite.setDefense((int) (spriteType.getBasicDefense() * scale));
        scale = 0.8 + GameCache.random.nextDouble() * 0.4;
        sprite.setSpeed((int) (spriteType.getBasicSpeed() * scale));
        scale = 0.8 + GameCache.random.nextDouble() * 0.4;
        sprite.setVisionRange((int) (spriteType.getBasicVisionRange() * scale));
        scale = 0.8 + GameCache.random.nextDouble() * 0.4;
        sprite.setAttackRange((int) (spriteType.getBasicAttackRange() * scale));
        sprite.setX(x);
        sprite.setY(y);
        // 宽度和高度使用相同的scale
        scale = 0.8 + GameCache.random.nextDouble() * 0.4;
        sprite.setWidth(spriteType.getBasicWidth() * scale);
        sprite.setHeight(spriteType.getBasicHeight() * scale);
        sprite.setMap(mapId);
        normalizeAndUpdateSprite(sprite);
        return sprite;
    }

    @Transactional
    public SpriteDo generateRandomSprite(SpriteTypeEnum type, String owner, double x, double y) {
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
            sprite.setX(GameCache.spriteCacheMap.get(sprite.getId()).getX());
            sprite.setY(GameCache.spriteCacheMap.get(sprite.getId()).getY());
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

        // 物品数目减1
        itemService.reduce(owner, itemId, 1);

        // 可能有精灵效果变化
        responseList.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(owner)));

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

        responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, new ItemBarNotifyVo(spriteId)));
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(spriteId)));

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

        responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, new ItemBarNotifyVo(spriteId)));
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(spriteId)));

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
            responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, new ItemBarNotifyVo(spriteId)));
        }
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(spriteId)));

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
            responses.add(new WSResponseVo(WSResponseEnum.ITEM_BAR_NOTIFY, new ItemBarNotifyVo(spriteId)));
        }
        // 可能有精灵效果变化
        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(spriteId)));

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
        spriteMapper.recoverSpritesLife(spriteIds, minHunger, incVal, Constants.MAX_HP);
    }

    @Transactional
    public List<WSResponseVo> attack(SpriteDo sourceSprite, SpriteDo targetSprite) {
        List<WSResponseVo> responses = new ArrayList<>();
        // 如果被攻击者有火焰护体效果，则攻击者烧伤
        if (targetSprite.getEffects().stream().anyMatch(effect -> effect.getEffect() == EffectEnum.FLAME_BODY)) {
            // 攻击者原先是否有烧伤效果
            SpriteEffectDo spriteEffectDo = spriteEffectMapper.selectBySpriteAndEffect(sourceSprite.getId(), EffectEnum.BURN);
            int burnTime = 8;
            if (spriteEffectDo != null && spriteEffectDo.getExpire() > System.currentTimeMillis()) {
                // 如果有未过期的效果，则更新效果
                spriteEffectDo.setDuration(spriteEffectDo.getDuration() + burnTime);
                spriteEffectDo.setExpire(spriteEffectDo.getExpire() + burnTime * 1000);
                spriteEffectMapper.update(spriteEffectDo);
            } else {
                // 如果没有，则添加效果
                spriteEffectDo = new SpriteEffectDo();
                spriteEffectDo.setSprite(sourceSprite.getId());
                spriteEffectDo.setEffect(EffectEnum.BURN);
                spriteEffectDo.setDuration(burnTime);
                spriteEffectDo.setExpire(System.currentTimeMillis() + burnTime * 1000);
                spriteEffectMapper.insertOrUpdate(spriteEffectDo);
            }
            responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(sourceSprite.getId())));
        }
        // 被攻击者以攻击者为目标
        SpriteCache cache = targetSprite.getCache();
        cache.setTargetSpriteId(sourceSprite.getId());
        // 计算伤害
        int damage = sourceSprite.getAttack() + sourceSprite.getAttackInc() -
                (targetSprite.getDefense() + targetSprite.getDefenseInc());
        if (damage > 0) {
            var modifyLifeResponses = modifyLife(targetSprite.getId(), -damage);
            responses.addAll(modifyLifeResponses);
            // 如果包含offline消息，则说明被攻击者死亡
            if (modifyLifeResponses.stream().anyMatch(response -> response.getType() == WSResponseEnum.OFFLINE)) {
                // 查询被攻击者死亡后带给攻击者的属性提升
                VictoryAttributeRewardDo attributeReward = victoryAttributeRewardMapper.selectById(targetSprite.getType());
                if (attributeReward != null) {
                    // 更新攻击者属性
                    SpriteAttributeChangeVo spriteAttributeChange = new SpriteAttributeChangeVo();
                    spriteAttributeChange.setOriginal(sourceSprite);
                    sourceSprite.setMoney(sourceSprite.getMoney() + attributeReward.getMoneyInc());
                    sourceSprite.setExp(sourceSprite.getExp() + attributeReward.getExpInc());
                    sourceSprite = normalizeAndUpdateSprite(sourceSprite).getFirst();
                    if (spriteAttributeChange.setChanged(sourceSprite)) {
                        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_ATTRIBUTE_CHANGE, spriteAttributeChange));
                    }
                }
                // 查询被攻击者死亡后带给攻击者的物品
                List<VictoryItemRewardDo> itemRewards = victoryItemRewardMapper.selectBySpriteType(targetSprite.getType());
                for (VictoryItemRewardDo itemReward : itemRewards) {
                    // 为玩家添加物品
                    int cnt = GameCache.random.nextInt(itemReward.getMinCount(), itemReward.getMaxCount() + 1);
                    if (cnt <= 0) {
                        continue;
                    }
                    itemService.add(sourceSprite.getId(), itemReward.getItemType(), cnt);
                    // 获得物品的通知
                    responses.add(new WSResponseVo(WSResponseEnum.ITEM_GAIN, new ItemGainVo(sourceSprite.getId(), itemReward.getItemType(), cnt)));
                }
            }
        }
        return responses;
    }

    /**
     * 使精灵下线
     */
    public WSResponseVo offline(String spriteId) {
        List<String> ids = new ArrayList<>();
        offline(spriteId, ids);
        // 使精灵下线
        ids.forEach(id -> GameCache.spriteCacheMap.remove(id));
        // 发送下线消息
        return new WSResponseVo(WSResponseEnum.OFFLINE, new OfflineVo(ids));
    }

    private void offline(String spriteId, List<String> ids) {
        ids.add(spriteId);
        // 读取精灵的所有宠物
        List<SpriteDo> pets = selectByOwner(spriteId);
        for (SpriteDo pet : pets) {
            if (pet.getType() == SpriteTypeEnum.USER) {
                continue;
            }
            ids.add(pet.getId());
        }
    }

    public void updatePosition(String id, double x, double y) {
        spriteMapper.updatePosition(id, x, y);
    }

    /**
     * 修改精灵生命
     */
    @Transactional
    public List<WSResponseVo> modifyLife(String spriteId, int val) {
        SpriteDo sprite = spriteMapper.selectById(spriteId);
        List<WSResponseVo> responses = new ArrayList<>();
        HpChangeVo hpChangeVo = new HpChangeVo();
        hpChangeVo.setId(sprite.getId());
        hpChangeVo.setOriginHp(sprite.getHp());
        // 扣除目标精灵生命
        sprite.setHp(sprite.getHp() + val);
        // 判断目标精灵是否死亡
        if (sprite.getHp() <= 0) {
            sprite.setHp(0);
        }
        // 如果满血
        if (sprite.getHp() > Constants.MAX_HP) {
            sprite.setHp(Constants.MAX_HP);
        }
        hpChangeVo.setHpChange(sprite.getHp() - hpChangeVo.getOriginHp());
        if (hpChangeVo.getHpChange() != 0) {
            responses.add(new WSResponseVo(WSResponseEnum.SPRITE_HP_CHANGE, hpChangeVo));
            // 更新目标精灵
            responses.addAll(normalizeAndUpdateSprite(sprite).getSecond());
        }
        return responses;
    }

    /** 判断两个精灵是否接近 */
    public boolean isNear(SpriteDo sprite1, SpriteDo sprite2) {
        // 之所以这里不乘以widthRatio和heightRatio，是因为这里是检测是否接近而不是检测是否碰撞，因此放宽一点要求
        return Math.abs(sprite1.getX() - sprite2.getX()) < (sprite1.getWidth() + sprite2.getWidth()) / 2 &&
                Math.abs(sprite1.getY() - sprite2.getY()) < (sprite1.getHeight() + sprite2.getHeight()) / 2;
    }

    /**
     * 驯服&喂养精灵
     *
     * @param sourceSprite 驯服/喂养者
     * @param targetSprite 被驯服/喂养者
     * @return 驯服结果
     */
    @Transactional
    public FeedResultEnum feed(SpriteDo sourceSprite, SpriteDo targetSprite) {
        // 判断是否可驯服
        List<FeedDo> feedList = feedMapper.selectBySpriteType(targetSprite.getType());
        if (feedList == null || feedList.isEmpty()) {
            return FeedResultEnum.CANNOT_TAMED;
        }
        // 是否手持了驯服所需物品
        List<ItemDo> handHeldItems = itemMapper.selectByOwnerAndPosition(sourceSprite.getId(), ItemPositionEnum.HANDHELD);
        if (handHeldItems == null || handHeldItems.isEmpty()) {
            return FeedResultEnum.NO_ITEM;
        }
        // 得到当前手持物品对应的驯服信息
        FeedDo feed = feedList.stream()
                .filter(f -> f.getItemType() == handHeldItems.get(0).getItemType())
                .findFirst()
                .orElse(null);
        if (feed == null) {
            return FeedResultEnum.NO_ITEM;
        }
        // 喂养目标精灵
        targetSprite.setHunger(targetSprite.getHunger() + feed.getHungerInc());
        targetSprite.setExp(targetSprite.getExp() + feed.getExpInc());
        // 从手持物品栏减少1个物品（如果是最后一个物品，则删除）
        itemService.reduce(sourceSprite.getId(), handHeldItems.get(0).getId(), 1);
        // 判断是否已经有主人
        if (targetSprite.getOwner() != null && !targetSprite.getOwner().equals(sourceSprite.getId())) {
            normalizeAndUpdateSprite(targetSprite);
            return FeedResultEnum.ALREADY_TAMED;
        }
        // 如果已经被自己驯服，那就直接返回喂养成功
        if (targetSprite.getOwner() != null) {
            normalizeAndUpdateSprite(targetSprite);
            return FeedResultEnum.FEED_SUCCESS;
        }
        // 否则是野生的，以一定概率驯服宠物
        if (GameCache.random.nextDouble() < feed.getTameProb()) {
            // 驯服成功
            targetSprite.setOwner(sourceSprite.getId());
            normalizeAndUpdateSprite(targetSprite);
            return FeedResultEnum.TAME_SUCCESS;
        } else {
            // 驯服失败
            normalizeAndUpdateSprite(targetSprite);
            return FeedResultEnum.TAME_FAIL;
        }
    }

    /**
     * 使精灵上线
     */
    public SpriteCache online(String id) {
        SpriteDo sprite = spriteMapper.selectById(id);
        // 将精灵的坐标信息写入缓存
        SpriteCache cache = new SpriteCache();
        cache.setX(sprite.getX());
        cache.setY(sprite.getY());
        cache.setLastMoveTime(System.currentTimeMillis());
        GameCache.spriteCacheMap.compute(id, (k, v) -> {
            // 如果已经在线，则不做任何操作
            if (v != null) {
                return v;
            }
            return cache;
        });
        // 使其宠物上线
        List<SpriteDo> pets = selectByOwner(id);
        pets.forEach(pet -> {
            // 如果宠物是玩家，那么不需要上线
            if (pet.getType() == SpriteTypeEnum.USER) {
                return;
            }
            online(pet.getId());
        });
        return cache;
    }

    /**
     * 刷新精灵
     *
     * @param time 只刷出该时间段的精灵
     */
    @Transactional
    public void refreshSprites(TimeFrameEnum time) {
        // 读取精灵刷新信息
        List<SpriteRefreshDo> spriteRefreshes = spriteRefreshMapper.selectByTime(time);
        // 按照精灵类型分组
        Map<SpriteTypeEnum, List<SpriteRefreshDo>> spriteRefreshMap = spriteRefreshes.stream()
                .collect(Collectors.groupingBy(SpriteRefreshDo::getSpriteType));
        // 对于每一种精灵
        for (var entry : spriteRefreshMap.entrySet()) {
            // 得到目前的数目
            int curNum = spriteMapper.countByTypeAndMap(entry.getKey(), mapId);
            // 将刷新信息按照建筑物类型分组
            Map<BuildingTypeEnum, SpriteRefreshDo> buildingTypeMap = entry.getValue().stream()
                    .collect(Collectors.toMap(SpriteRefreshDo::getBuildingType, Function.identity()));
            // 得到所有的建筑
            List<BuildingDo> buildings = buildingMapper.selectByMapIdAndTypes(mapId, entry.getValue().stream()
                    .map(SpriteRefreshDo::getBuildingType).collect(Collectors.toList()));
            // 打乱建筑顺序
            Collections.shuffle(buildings);
            // 遍历每一个建筑，生成精灵
            for (BuildingDo building : buildings) {
                SpriteRefreshDo spriteRefresh = buildingTypeMap.get(building.getType());
                // 生成数目
                int cnt = GameCache.random.nextInt(spriteRefresh.getMinCount(),
                        spriteRefresh.getMaxCount() + 1);
                if (cnt <= 0) {
                    continue;
                }
                if (curNum >= cnt) {
                    curNum -= cnt;
                    continue;
                } else if (curNum > 0) {
                    cnt -= curNum;
                    curNum = 0;
                }
                for (int i = 0; i < cnt; ++i) {
                    // 随机生成精灵的左上角
                    double spriteX = building.getOriginX() + GameCache.random.nextDouble() * building.getWidth();
                    double spriteY = building.getOriginY() + GameCache.random.nextDouble() * building.getHeight();
                    // 创建精灵
                    SpriteDo sprite = generateRandomSprite(entry.getKey(), null, spriteX, spriteY);
                    // 使精灵上线
                    online(sprite.getId());
                }
            }
        }
    }
}
