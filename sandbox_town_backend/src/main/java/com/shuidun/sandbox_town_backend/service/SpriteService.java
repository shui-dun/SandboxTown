package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.*;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.*;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.MyMath;
import com.shuidun.sandbox_town_backend.utils.UUIDNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SpriteService {
    /** 升级所需经验值的基数 */
    private final int EXP_PER_LEVEL = 100;

    /** 精灵最大体力值 */
    private final int MAX_HP = 100;

    /** 精灵最大等级 */
    private final int MAX_LEVEL = 20;

    /** 精灵最大速度 */
    private final int MAX_SPEED = 25;

    /** 玩家死亡时失去的金钱值 */
    private final int MONEY_LOST_ON_DEATH = 120;

    /** 精灵升级时得到的金钱值 */
    private final int MONEY_GAIN_ON_LEVEL_UP = 50;

    /** 精灵最大饥饿值 */
    private final int MAX_HUNGER = 100;

    /** 精灵饥饿值的临界点（低于这个值就不会自动恢复体力） */
    private final int HUNGER_THRESHOLD = 80;

    private final SpriteMapper spriteMapper;

    private final SpriteTypeMapper spriteTypeMapper;

    private final ItemService itemService;

    private final ItemMapper itemMapper;

    private final BuildingMapper buildingMapper;

    private final SpriteRefreshMapper spriteRefreshMapper;

    private final FeedMapper feedMapper;

    private final VictoryAttributeRewardMapper victoryAttributeRewardMapper;

    private final VictoryItemRewardMapper victoryItemRewardMapper;

    private final EffectService effectService;

    private MapService mapService;

    /**
     * 在线精灵信息
     * 用户只有在登录后才会在线，其他精灵只要存在就会在线
     */
    private final Map<String, SpriteBo> onlineSpriteMap = new ConcurrentHashMap<>();

    @Value("${mapId}")
    private String mapId;

    public SpriteService(SpriteMapper spriteMapper, SpriteTypeMapper spriteTypeMapper, ItemService itemService, ItemMapper itemMapper, BuildingMapper buildingMapper, SpriteRefreshMapper spriteRefreshMapper, FeedMapper feedMapper, VictoryAttributeRewardMapper victoryAttributeRewardMapper, VictoryItemRewardMapper victoryItemRewardMapper, EffectService effectService) {
        this.spriteMapper = spriteMapper;
        this.spriteTypeMapper = spriteTypeMapper;
        this.itemService = itemService;
        this.itemMapper = itemMapper;
        this.buildingMapper = buildingMapper;
        this.spriteRefreshMapper = spriteRefreshMapper;
        this.feedMapper = feedMapper;
        this.victoryAttributeRewardMapper = victoryAttributeRewardMapper;
        this.victoryItemRewardMapper = victoryItemRewardMapper;
        this.effectService = effectService;
    }

    /** 为精灵设置缓存信息，包含类型信息、装备、属性增量信息和效果列表 */
    private SpriteBo assignCacheToSprite(SpriteBo sprite) {
        sprite.setSpriteTypeDo(spriteTypeMapper.selectById(sprite.getType()));
        // 获取装备列表
        List<ItemDetailBo> equipments = itemService.listItemsInEquipmentByOwnerWithDetail(sprite.getId());
        // 设置效果列表
        List<SpriteEffectWithEffectBo> effects = effectService.listSpriteEffectsBySpriteIdAndEquipments(sprite.getId(), equipments);
        // 设置装备列表
        sprite.setEquipments(equipments);
        // 设置属性增量信息
        // 首先将属性增强全都设置为0
        sprite.setHungerInc(0);
        sprite.setHpInc(0);
        sprite.setAttackInc(0);
        sprite.setDefenseInc(0);
        sprite.setSpeedInc(0);
        sprite.setVisionRangeInc(0);
        sprite.setAttackRangeInc(0);
        // 对于所有装备，计算属性增量
        for (ItemDetailBo item : sprite.getEquipments()) {
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
        sprite.setEffects(effects);
        // 设置在线相关缓存
        if (sprite.getVx() == null) {
            sprite.setVx(0.0);
        }
        if (sprite.getVy() == null) {
            sprite.setVy(0.0);
        }
        if (sprite.getLastMoveTime() == null) {
            sprite.setLastMoveTime(System.currentTimeMillis());
        }
        return sprite;
    }

    /**
     * 根据id获取角色详细信息（带有类型信息、装备信息、属性增量信息、效果列表信息）
     * 精灵不存在，返回null
     */
    @Nullable
    public SpriteBo selectById(String id) {
        SpriteBo sprite = onlineSpriteMap.get(id);

        if (sprite == null) {
            SpriteDo spriteDo = spriteMapper.selectById(id);
            if (spriteDo == null) {
                return null;
            }
            sprite = SpriteBo.fromSpriteDo(spriteDo);
        }
        return assignCacheToSprite(sprite);
    }

    /**
     * 根据id获取角色详细信息（带有类型信息、装备信息、属性增量信息、效果列表信息）
     * 精灵不在线，返回null
     */
    @Nullable
    public SpriteBo selectOnlineById(String id) {
        SpriteBo spriteBo = onlineSpriteMap.get(id);
        if (spriteBo == null) {
            return null;
        }
        return assignCacheToSprite(spriteBo);
    }

    /** 判断角色属性值是否在合理范围内（包含升级操作） */
    @Transactional
    public Pair<SpriteDo, List<WSResponseVo>> normalizeAndUpdateSprite(SpriteDo sprite) {
        List<WSResponseVo> responseList = new ArrayList<>();
        // 如果精灵已经满级
        if (sprite.getLevel().equals(MAX_LEVEL)) {
            sprite.setExp(0);
        } else {
            // 如果经验值足够升级，则升级（当精灵等级为n时，升级所需经验值为n*EXP_PER_LEVEL）
            int levelUp = 0;
            while (sprite.getExp() >= EXP_PER_LEVEL * sprite.getLevel()) {
                // 减少经验值
                sprite.setExp(sprite.getExp() - EXP_PER_LEVEL * sprite.getLevel());
                // 升级
                sprite.setLevel(sprite.getLevel() + 1);
                levelUp++;
                // 如果精灵已经满级
                if (sprite.getLevel().equals(MAX_LEVEL)) {
                    sprite.setExp(0);
                }
            }
            if (levelUp > 0) {
                // 得到类型信息
                SpriteTypeDo spriteType = spriteTypeMapper.selectById(sprite.getType());
                assert spriteType != null;
                // 更新玩家属性
                sprite.setMoney(sprite.getMoney() + levelUp * MONEY_GAIN_ON_LEVEL_UP);
                sprite.setHunger(MAX_HUNGER);
                sprite.setHp(MAX_HP);
                // 每次升级，攻击和防御增加基础属性值的1/4，至少增加1
                sprite.setAttack(sprite.getAttack() + Math.max(1, spriteType.getBasicAttack() / 4) * levelUp);
                sprite.setDefense(sprite.getDefense() + Math.max(1, spriteType.getBasicDefense() / 4) * levelUp);
                // 每升一级，速度只能增加1
                sprite.setSpeed(sprite.getSpeed() + levelUp);
                // 每次升级，视野和攻击范围增加基础属性值的1/30，至少增加1
                sprite.setVisionRange(sprite.getVisionRange() + Math.max(1, spriteType.getBasicVisionRange() / 30) * levelUp);
                sprite.setAttackRange(sprite.getAttackRange() + Math.max(1, spriteType.getBasicAttackRange() / 30) * levelUp);
            }
        }
        // 判断属性是否在合理范围内
        if (sprite.getHunger() > MAX_HUNGER) {
            sprite.setHunger(MAX_HUNGER);
        }
        if (sprite.getHunger() < 0) {
            sprite.setHunger(0);
        }
        if (sprite.getHp() > MAX_HP) {
            sprite.setHp(MAX_HP);
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
        // 速度上限
        if (sprite.getSpeed() > MAX_SPEED) {
            sprite.setSpeed(MAX_SPEED);
        }
        if (sprite.getVisionRange() < 0) {
            sprite.setVisionRange(0);
        }
        if (sprite.getAttackRange() < 0) {
            sprite.setAttackRange(0);
        }
        // 如果精灵不在线，则写入数据库
        if (!onlineSpriteMap.containsKey(sprite.getId())) {
            spriteMapper.insertOrUpdateById(sprite);
        }
        // 判断是否死亡
        if (sprite.getHp() == 0) {
            // 如果是玩家，则扣除金钱和清除经验，恢复饱腹值和生命值，并设置坐标为原点
            if (sprite.getType() == SpriteTypeEnum.USER) {
                sprite.setMoney(Math.max(0, sprite.getMoney() - MONEY_LOST_ON_DEATH));
                sprite.setExp(0);
                sprite.setHunger(MAX_HUNGER);
                sprite.setHp(MAX_HP);
                sprite.setX(0.0);
                sprite.setY(0.0);
                // 如果在线，设置坐标为原点
                if (onlineSpriteMap.containsKey(sprite.getId())) {
                    sprite.setX(0.0);
                    sprite.setY(0.0);
                    // 修复玩家死亡之后有可能位置不变，没有回到出生点的bug
                    var spriteBo = selectOnlineById(sprite.getId());
                    assert spriteBo != null;
                    spriteBo.setLastMoveTime(System.currentTimeMillis() + 500);
                    responseList.add(new WSResponseVo(WSResponseEnum.COORDINATE, new CoordinateVo(
                            sprite.getId(),
                            0.0, 0.0, 0.0, 0.0
                    )));
                }
            } else { // 否则，删除
                // 使精灵下线
                responseList.add(offline(sprite.getId()));
            }
        }
        return Pair.of(sprite, responseList);
    }

    /** 生成固定的（即各属性值严格等于其精灵类型的基础属性值）指定类型的角色，并写入数据库 */
    @Transactional
    public SpriteDo generateFixedSprite(SpriteTypeEnum type, String id, @Nullable String owner, double x, double y) {
        SpriteDo sprite = new SpriteDo();
        SpriteTypeDo spriteType = spriteTypeMapper.selectById(type);
        assert spriteType != null;
        sprite.setId(id);
        sprite.setType(type);
        sprite.setOwner(owner);
        sprite.setMoney(spriteType.getBasicMoney());
        sprite.setExp(spriteType.getBasicExp());
        sprite.setLevel(spriteType.getBasicLevel());
        // 将等级降为1，全部赋给经验值
        if (sprite.getLevel() > 1) {
            sprite.setExp(sprite.getExp() + (sprite.getLevel() - 1) * sprite.getLevel() * EXP_PER_LEVEL / 2);
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
    public SpriteDo generateRandomSprite(SpriteTypeEnum type, String id, @Nullable String owner, double x, double y) {
        SpriteDo sprite = new SpriteDo();
        SpriteTypeDo spriteType = spriteTypeMapper.selectById(type);
        assert spriteType != null;
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
            sprite.setExp(sprite.getExp() + (sprite.getLevel() - 1) * sprite.getLevel() * EXP_PER_LEVEL / 2);
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
    public SpriteDo generateRandomSprite(SpriteTypeEnum type, @Nullable String owner, double x, double y) {
        String id = UUIDNameGenerator.generateItemName(type.name());
        return generateRandomSprite(type, id, owner, x, y);
    }

    public Map<String, SpriteBo> getOnlineSprites() {
        return onlineSpriteMap;
    }

    /**
     * @param n        取1/n的精灵
     * @param curFrame 当前帧数
     */
    public List<SpriteBo> getOnlineSpritesByFrame(int n, long curFrame) {
        return getOnlineSprites().values().stream()
                .filter(s -> MyMath.safeMod(s.getId().hashCode(), n) == MyMath.safeMod(curFrame, n))
                .toList();
    }

    /** 将NPC精灵上线 */
    public void onlineNPCs() {
        List<SpriteDo> npcs = spriteMapper.selectNPCs();
        for (SpriteDo sprite : npcs) {
            online(sprite.getId());
        }
    }

    /**
     * 使用物品
     * 按理来说，这个函数应该在ItemService中，但是对物品的使用也会对角色（例如精灵的属性）产生影响
     * 如果将这个函数放在ItemService中，ItemService就会依赖SpriteService对精灵（例如精灵属性）进行修改
     * 而SpriteService又依赖ItemService来查看精灵的物品
     * 这样就会造成循环依赖，因此在找到更好地解决方法前，将这个函数放在SpriteService中
     */
    @Transactional
    public List<WSResponseVo> useItem(String owner, String itemId) {
        List<WSResponseVo> responseList = new ArrayList<>();
        // 判断物品是否存在
        ItemDetailBo item = itemService.getItemDetailById(itemId);
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断角色是否存在
        SpriteDo sprite = selectById(owner);
        if (sprite == null) {
            throw new BusinessException(StatusCodeEnum.SPRITE_NOT_FOUND);
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
        // 向角色施加效果
        // var newEffects = item.getItemTypeObj().getEffects().get(ItemOperationEnum.USE).values();
        // 为避免空指针异常，改为：
        var newEffects = item.getItemTypeObj().getEffects().getOrDefault(ItemOperationEnum.USE, new HashMap<>()).values();
        for (ItemTypeEffectDo effect : newEffects) {
            effectService.addEffect(owner, effect.getEffect(), effect.getDuration());
        }

        // 物品数目减1
        itemService.reduce(owner, itemId, 1);

        // 可能有精灵效果变化
        responseList.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(owner)));

        return responseList;
    }

    /**
     * 减少精灵饱腹值
     *
     * @param spriteIds 精灵id集合
     * @param val       减少值
     */
    public void reduceSpritesHunger(Collection<String> spriteIds, int val) {
        for (String spriteId : spriteIds) {
            SpriteBo sprite = selectOnlineById(spriteId);
            if (sprite == null) {
                continue;
            }
            sprite.setHunger(Math.max(0, sprite.getHunger() - val));
            normalizeAndUpdateSprite(sprite);
        }
    }

    /**
     * 恢复精灵生命
     *
     * @param spriteIds 精灵id集合
     * @param incVal    恢复值
     */
    public void recoverSpritesLife(Collection<String> spriteIds, int incVal) {
        for (String spriteId : spriteIds) {
            SpriteBo sprite = selectOnlineById(spriteId);
            if (sprite == null) {
                continue;
            }
            sprite.setHp(Math.min(sprite.getHp() + incVal, MAX_HP));
            normalizeAndUpdateSprite(sprite);
        }
    }

    @Transactional
    public List<WSResponseVo> attack(SpriteBo sourceSprite, SpriteBo targetSprite) {
        // 如果双方有人不在线，则不进行攻击
        if (!onlineSpriteMap.containsKey(sourceSprite.getId()) || !onlineSpriteMap.containsKey(targetSprite.getId())) {
            return Collections.emptyList();
        }
        List<WSResponseVo> responses = new ArrayList<>();
        // 如果被攻击者有火焰护体效果，则攻击者烧伤
        if (targetSprite.getEffects().stream().anyMatch(effect -> effect.getEffect() == EffectEnum.FLAME_BODY)) {
            // 添加8秒的烧伤效果
            effectService.addEffect(sourceSprite.getId(), EffectEnum.BURN, 8);
            responses.add(new WSResponseVo(WSResponseEnum.SPRITE_EFFECT_CHANGE, new SpriteEffectChangeVo(sourceSprite.getId())));
        }
        // 被攻击者以攻击者为目标
        targetSprite.setTargetSpriteId(sourceSprite.getId());
        // 攻击者也以被攻击者为目标
        sourceSprite.setTargetSpriteId(targetSprite.getId());
        // 计算伤害
        int damage = sourceSprite.getAttack() + sourceSprite.getAttackInc() -
                (targetSprite.getDefense() + targetSprite.getDefenseInc());
        if (damage > 0) {
            var modifyLifeResponses = modifyLife(targetSprite.getId(), -damage);
            responses.addAll(modifyLifeResponses);
            // 如果包含offline消息，则说明被攻击者死亡
            if (modifyLifeResponses.stream().anyMatch(response -> response.getType() == WSResponseEnum.OFFLINE)) {
                // 获得攻击者的主人
                String ownerId = sourceSprite.getOwner();
                SpriteDo owner = ownerId == null ? null : selectById(ownerId);
                // 查询被攻击者死亡后带给攻击者的属性提升（属性提升值不仅与死亡者的类型有关，还与死亡者的等级有关）
                VictoryAttributeRewardDo attributeReward = victoryAttributeRewardMapper.selectById(targetSprite.getType());
                if (attributeReward != null) {
                    // 死亡者的等级带来的属性增益因数
                    double levelFactor = 1 + (targetSprite.getLevel() - 1) * 0.2;
                    // 带来的金钱和经验
                    int moneyInc = (int) (attributeReward.getMoneyInc() * levelFactor);
                    int expInc = (int) (attributeReward.getExpInc() * levelFactor);
                    // 更新攻击者属性
                    SpriteAttributeChangeVo spriteAttributeChange = new SpriteAttributeChangeVo();
                    spriteAttributeChange.setOriginal(sourceSprite);
                    sourceSprite.setMoney(sourceSprite.getMoney() + moneyInc);
                    sourceSprite.setExp(sourceSprite.getExp() + expInc);
                    SpriteDo newSourceSprite = normalizeAndUpdateSprite(sourceSprite).getFirst();
                    if (spriteAttributeChange.setChanged(newSourceSprite)) {
                        responses.add(new WSResponseVo(WSResponseEnum.SPRITE_ATTRIBUTE_CHANGE, spriteAttributeChange));
                    }
                    // 攻击者主人的属性也得到同样的提升
                    if (owner != null) {
                        SpriteAttributeChangeVo ownerAttributeChange = new SpriteAttributeChangeVo();
                        ownerAttributeChange.setOriginal(owner);
                        owner.setMoney(owner.getMoney() + moneyInc);
                        owner.setExp(owner.getExp() + expInc);
                        owner = normalizeAndUpdateSprite(owner).getFirst();
                        if (ownerAttributeChange.setChanged(owner)) {
                            responses.add(new WSResponseVo(WSResponseEnum.SPRITE_ATTRIBUTE_CHANGE, ownerAttributeChange));
                        }
                    }
                }
                // 查询被攻击者死亡后带来的物品奖励
                List<VictoryItemRewardDo> itemRewards = victoryItemRewardMapper.selectBySpriteType(targetSprite.getType());
                // 如果攻击者没有主人，物品归攻击者所有，否则归主人所有
                String itemOwner = (owner == null ? sourceSprite.getId() : owner.getId());
                for (VictoryItemRewardDo itemReward : itemRewards) {
                    // 为玩家添加物品
                    int cnt = GameCache.random.nextInt(itemReward.getMinCount(), itemReward.getMaxCount() + 1);
                    if (cnt <= 0) {
                        continue;
                    }
                    itemService.add(itemOwner, itemReward.getItemType(), cnt);
                    // 获得物品的通知
                    responses.add(new WSResponseVo(WSResponseEnum.ITEM_GAIN, new ItemGainVo(itemOwner, itemReward.getItemType(), cnt)));
                }
            }
        }
        return responses;
    }

    /**
     * 使精灵下线
     * 精灵下线只会下线自己，不会下线自己的宠物
     */
    public WSResponseVo offline(String spriteId) {
        var msg = new WSResponseVo(WSResponseEnum.OFFLINE, new OfflineVo(List.of(spriteId)));
        SpriteBo s = onlineSpriteMap.get(spriteId);
        if (s == null) {
            return msg;
        }
        onlineSpriteMap.remove(s.getId());
        // 精灵下线，信息需要立即写入数据库
        if (s.getHp() != 0 || s.getType() == SpriteTypeEnum.USER) {
            spriteMapper.updateById(s);
        } else {
            // 删除精灵
            spriteMapper.deleteById(spriteId);
        }
        // 发送下线消息
        return msg;
    }

    /**
     * 修改精灵生命
     */
    @Transactional
    public List<WSResponseVo> modifyLife(String spriteId, int val) {
        SpriteDo sprite = selectById(spriteId);
        if (sprite == null) {
            throw new BusinessException(StatusCodeEnum.SPRITE_NOT_FOUND);
        }
        List<WSResponseVo> responses = new ArrayList<>();
        int originHp = sprite.getHp();
        // 扣除目标精灵生命
        sprite.setHp(sprite.getHp() + val);
        // 判断目标精灵是否死亡
        if (sprite.getHp() <= 0) {
            sprite.setHp(0);
        }
        // 如果满血
        if (sprite.getHp() > MAX_HP) {
            sprite.setHp(MAX_HP);
        }
        HpChangeVo hpChangeVo = new HpChangeVo(spriteId, originHp, sprite.getHp() - originHp);
        if (hpChangeVo.getHpChange() != 0) {
            responses.add(new WSResponseVo(WSResponseEnum.SPRITE_HP_CHANGE, hpChangeVo));
            // 更新目标精灵
            responses.addAll(normalizeAndUpdateSprite(sprite).getSecond());
        }
        return responses;
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
        if (feedList.isEmpty()) {
            return FeedResultEnum.CANNOT_TAMED;
        }
        // 是否手持了驯服所需物品
        List<ItemDo> handHeldItems = itemMapper.selectByOwnerAndPosition(sourceSprite.getId(), ItemPositionEnum.HANDHELD);
        if (handHeldItems.isEmpty()) {
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
     * 使精灵上线（如果已经上线，则不做任何操作）
     * 如果精灵不存在，返回null
     */
    @Nullable
    public SpriteBo online(String id) {
        SpriteBo sprite = onlineSpriteMap.get(id);
        if (sprite != null) {
            return sprite;
        }
        sprite = selectById(id);
        if (sprite == null) {
            return null;
        }
        onlineSpriteMap.put(id, sprite);
        return sprite;
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
            int curNum = (int) countByType(entry.getKey());
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
                assert spriteRefresh != null;
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

    /** 根据精灵类型得到精灵数量 */
    private long countByType(SpriteTypeEnum type) {
        return onlineSpriteMap.values().stream()
                .filter(s -> s.getType() == type)
                .count();
    }

    /** 得到指定类型的精灵 */
    private List<SpriteBo> selectByTypes(List<SpriteTypeEnum> types) {
        return onlineSpriteMap.values().stream()
                .filter(s -> types.contains(s.getType()))
                .toList();
    }

    /** 刷新所有时间段的精灵 */
    @Transactional
    public void refreshAllSprites() {
        for (TimeFrameEnum time : TimeFrameEnum.values()) {
            refreshSprites(time);
        }
    }

    /** 黎明时，所有夜行动物（即在晚上出现的动物）都会受到烧伤效果，直到黄昏到来 */
    public void burnAllNightSprites() {
        // 得到所有夜行动物类型
        List<SpriteTypeEnum> spriteTypes = spriteRefreshMapper.selectSpriteTypesByTime(TimeFrameEnum.NIGHT);
        // 得到所有夜行动物
        List<SpriteBo> sprites = selectByTypes(spriteTypes);
        // 为所有夜行动物添加烧伤效果
        for (SpriteDo sprite : sprites) {
            effectService.addEffect(sprite.getId(), EffectEnum.BURN, (int) (Constants.DAY_DURATION / 1000L));
        }
    }

    /**
     * 根据精灵名称判断精灵是否属于某种类型
     *
     * @param spriteName 精灵名称
     * @param type       精灵类型
     * @return 是否属于该类型
     */
    public boolean isSpriteType(String spriteName, SpriteTypeEnum type) {
        return spriteName.startsWith(type.name());
    }

    public boolean isOnline(String id) {
        return onlineSpriteMap.containsKey(id);
    }

    /** 定期写入数据库 */
    @Scheduled(fixedRate = 1000)
    public void saveOnlineSprites() {
        for (SpriteBo sprite : onlineSpriteMap.values()) {
            spriteMapper.updateById(sprite);
        }
    }

    /** 得到随机移动速度 */
    public Pair<Double, Double> randomVelocity(SpriteBo sprite) {
        double coefficient = 0.9;
        double randomVx = coefficient * (sprite.getSpeed() + sprite.getSpeedInc()) * (Math.random() - 0.5);
        double randomVy = coefficient * (sprite.getSpeed() + sprite.getSpeedInc()) * (Math.random() - 0.5);
        return Pair.of(randomVx, randomVy);
    }

    /** 得到精灵合法的目标，即目标精灵必须存在，并且在线，并且在视野范围内。如果不合法，则返回null */
    public Optional<SpriteBo> getValidTarget(SpriteBo sprite) {
        // 如果精灵本身不在线
        if (!isOnline(sprite.getId())) {
            return Optional.empty();
        }
        // 如果精灵的目标精灵不存在
        String targetSpriteId = sprite.getTargetSpriteId();
        if (targetSpriteId == null) {
            return Optional.empty();
        }
        // 如果目标精灵不在线
        SpriteBo targetSprite = selectOnlineById(targetSpriteId);
        if (targetSprite == null) {
            sprite.setTargetSpriteId(null);
            return Optional.empty();
        }
        // 如果目标精灵不在视野范围内
        if (!mapService.isInSight(sprite, targetSprite.getX(), targetSprite.getY())) {
            sprite.setTargetSpriteId(null);
            return Optional.empty();
        }
        return Optional.of(targetSprite);
    }

    /** 得到精灵合法的目标，并且以一定概率忘记目标 */
    public Optional<SpriteBo> getValidTargetWithRandomForget(SpriteBo sprite, double forgetProbability) {
        Optional<SpriteBo> targetSprite = getValidTarget(sprite);
        // 如果目标精灵不合法
        if (targetSprite.isEmpty()) {
            return Optional.empty();
        }
        // 以一定概率忘记目标
        if (GameCache.random.nextDouble() < forgetProbability) {
            sprite.setTargetSpriteId(null);
            return Optional.empty();
        }
        return targetSprite;
    }

    /** 得到精灵的合法主人，即主人必须存在，并且在线，并且在视野范围内。如果不合法，则返回null */
    public Optional<SpriteBo> getValidOwner(SpriteBo sprite) {
        // 如果精灵本身不在线
        if (!isOnline(sprite.getId())) {
            return Optional.empty();
        }
        // 如果精灵的主人不存在
        String owner = sprite.getOwner();
        if (owner == null) {
            return Optional.empty();
        }
        // 如果主人不在线
        SpriteBo ownerSprite = selectOnlineById(owner);
        if (ownerSprite == null) {
            return Optional.empty();
        }
        // 如果主人不在视野范围内
        if (!mapService.isInSight(sprite, ownerSprite.getX(), ownerSprite.getY())) {
            return Optional.empty();
        }
        return Optional.of(ownerSprite);
    }

    @Autowired
    @Lazy
    public void setMapService(MapService mapService) {
        this.mapService = mapService;
    }
}
