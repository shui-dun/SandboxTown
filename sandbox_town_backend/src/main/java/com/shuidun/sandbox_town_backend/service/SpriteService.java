package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.*;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.BuildingMapper;
import com.shuidun.sandbox_town_backend.mapper.SpriteMapper;
import com.shuidun.sandbox_town_backend.mapper.SpriteRefreshMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.Concurrent;
import com.shuidun.sandbox_town_backend.utils.DataCompressor;
import com.shuidun.sandbox_town_backend.utils.MyMath;
import com.shuidun.sandbox_town_backend.utils.UUIDNameGenerator;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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

    private final SpriteTypeService spriteTypeService;

    private final ItemService itemService;

    private final BuildingMapper buildingMapper;

    private final SpriteRefreshMapper spriteRefreshMapper;

    private final FeedService feedService;

    private final VictoryRewardService victoryRewardService;

    @Lazy
    @Autowired
    private EffectService effectService;

    private MapService mapService;

    /**
     * 在线精灵信息
     * 用户只有在登录后才会在线，其他精灵只要存在就会在线
     */
    private final Map<String, SpriteBo> onlineSpriteMap = new ConcurrentHashMap<>();

    @Value("${mapId}")
    private String mapId;

    public SpriteService(SpriteMapper spriteMapper, SpriteTypeService spriteTypeService, ItemService itemService, BuildingMapper buildingMapper, SpriteRefreshMapper spriteRefreshMapper, FeedService feedService, VictoryRewardService victoryRewardService) {
        this.spriteMapper = spriteMapper;
        this.spriteTypeService = spriteTypeService;
        this.itemService = itemService;
        this.buildingMapper = buildingMapper;
        this.spriteRefreshMapper = spriteRefreshMapper;
        this.feedService = feedService;
        this.victoryRewardService = victoryRewardService;
    }

    /** 检测异常缓存 */
    private void checkAbnormalCache(SpriteBo sprite) {
        if (sprite.isDirty()) {
            return;
        }
        // 在程序出现错误时，非常极端的情况下，可能会出现有多个手持物品的情况
        // 这里将删掉所有手持物品，并标记缓存无效
        List<ItemBo> handHeldItems = sprite.getEquipments().stream()
                .filter(item -> item.getPosition() == ItemPositionEnum.HANDHELD)
                .toList();
        if (handHeldItems.size() > 1) {
            log.error("精灵{}有多个手持物品：{}", sprite.getId(), handHeldItems);
            for (ItemBo item : handHeldItems) {
                item.setPosition(ItemPositionEnum.BACKPACK);
                itemService.updateItem(item);
            }
            sprite.setDirty(true);
        }
    }

    /** 为精灵设置缓存信息，包含类型信息、装备、属性增量信息和效果列表 */
    private SpriteBo assignCacheToSprite(SpriteBo sprite) {
        checkAbnormalCache(sprite);
        if (!sprite.isDirty()) {
            // 去掉过期的效果
            sprite.setEffects(sprite.getEffects().stream()
                    .filter(e -> e.getExpire() == -1 || e.getExpire() >= System.currentTimeMillis())
                    .toList()
            );
            return sprite;
        }
        sprite.setSpriteTypeDo(spriteTypeService.selectById(sprite.getType()));
        // 获取装备列表
        List<ItemBo> equipments = itemService.listItemsInEquipmentByOwner(sprite.getId());
        // 设置效果列表
        List<SpriteEffectBo> effects = effectService.listSpriteEffectsBySpriteIdAndEquipments(sprite.getId(), equipments);
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
        for (ItemBo item : sprite.getEquipments()) {
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
        if (sprite.getLastMoveTime() == null) {
            sprite.setLastMoveTime(System.currentTimeMillis());
        }
        sprite.setDirty(false);
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
    public SpriteDo normalizeAndUpdateSprite(SpriteDo sprite) {
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
                SpriteTypeDo spriteType = spriteTypeService.selectById(sprite.getType());
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
                    // 修复玩家死亡之后有可能位置不变，没有回到出生点的bug
                    var spriteBo = selectOnlineById(sprite.getId());
                    assert spriteBo != null;
                    spriteBo.setX(0.0);
                    spriteBo.setY(0.0);
                    spriteBo.setLastMoveTime(System.currentTimeMillis() + 500);
                    coordinate(spriteBo);
                }
            } else { // 否则，删除
                // 使精灵下线
                offline(sprite.getId());
            }
        }
        return sprite;
    }

    /** 生成固定的（即各属性值严格等于其精灵类型的基础属性值）指定类型的角色，并写入数据库 */
    @Transactional
    public SpriteDo generateFixedSprite(SpriteTypeEnum type, String id, @Nullable String owner, double x, double y) {
        SpriteDo sprite = new SpriteDo();
        SpriteTypeDo spriteType = spriteTypeService.selectById(type);
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
        SpriteTypeDo spriteType = spriteTypeService.selectById(type);
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
                .collect(Collectors.toList());
    }

    /** 得到所有在线的用户 */
    public List<SpriteBo> onlineUsers() {
        return onlineSpriteMap.values().stream()
                .filter(s -> s.getType() == SpriteTypeEnum.USER)
                .collect(Collectors.toList());
    }

    /** 将NPC精灵上线 */
    public void onlineNPCs() {
        List<SpriteDo> npcs = spriteMapper.selectNPCs();
        for (SpriteDo sprite : npcs) {
            online(sprite.getId());
        }
    }

    /**
     * 减少精灵饱腹值
     */
    public void reduceSpritesHunger(Collection<SpriteBo> sprites) {
        Concurrent.executeInThreadPool(sprites, (sprite) -> {
            sprite.setHunger(Math.max(0, sprite.getHunger() - 1));
            normalizeAndUpdateSprite(sprite);
        });
    }

    /**
     * 恢复精灵生命
     */
    public void recoverSpritesLife(Collection<SpriteBo> sprites) {
        Concurrent.executeInThreadPool(sprites, (sprite) -> {
            if (sprite.getHunger() < HUNGER_THRESHOLD) {
                return;
            }
            modifyLife(sprite.getId(), 1);
        });
    }

    @Transactional
    public void attack(SpriteBo sourceSprite, SpriteBo targetSprite) {
        // 如果双方有人不在线，则不进行攻击
        if (!onlineSpriteMap.containsKey(sourceSprite.getId()) || !onlineSpriteMap.containsKey(targetSprite.getId())) {
            return;
        }
        // 如果双方有主仆关系，则不进行攻击
        if (sourceSprite.getOwner() != null && sourceSprite.getOwner().equals(targetSprite.getId())
                || targetSprite.getOwner() != null && targetSprite.getOwner().equals(sourceSprite.getId())) {
            WSMessageSender.addResponse(WSResponseEnum.CUSTOM_NOTIFICATION,
                    new CustomNotificationVo(sourceSprite.getId(), "不能攻击自己的伙伴"));
            return;
        }
        // 如果被攻击者有火焰护体效果，则攻击者烧伤
        if (hasEffect(targetSprite.getId(), EffectEnum.FLAME_BODY)) {
            // 添加8秒的烧伤效果
            effectService.addEffect(sourceSprite.getId(), EffectEnum.BURN, 8);
        }
        // 被攻击者进行反应
        var targetAgent = spriteAgentMap.get(targetSprite.getType());
        if (targetAgent != null) {
            targetAgent.onAttacked(targetSprite, sourceSprite);
        }
        // 计算伤害
        int damage = sourceSprite.getAttack() + sourceSprite.getAttackInc() -
                (targetSprite.getDefense() + targetSprite.getDefenseInc());
        if (damage > 0) {
            boolean dead = modifyLife(targetSprite.getId(), -damage);
            // 如果包含offline消息，则说明被攻击者死亡
            if (dead) {
                // 获得攻击者的主人
                String ownerId = sourceSprite.getOwner();
                SpriteDo owner = ownerId == null ? null : selectById(ownerId);
                // 查询奖励
                VictoryRewardBo victoryReward = victoryRewardService.selectBySpriteType(targetSprite.getType());
                // 被攻击者死亡后带给攻击者的属性提升（属性提升值不仅与死亡者的类型有关，还与死亡者的等级有关）
                VictoryAttributeRewardDo attributeReward = victoryReward.getAttributeReward();
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
                    SpriteDo newSourceSprite = normalizeAndUpdateSprite(sourceSprite);
                    if (spriteAttributeChange.setChanged(newSourceSprite)) {
                        WSMessageSender.addResponse(WSResponseEnum.SPRITE_ATTRIBUTE_CHANGE, spriteAttributeChange);
                    }
                    // 攻击者主人的属性也得到同样的提升
                    if (owner != null) {
                        SpriteAttributeChangeVo ownerAttributeChange = new SpriteAttributeChangeVo();
                        ownerAttributeChange.setOriginal(owner);
                        owner.setMoney(owner.getMoney() + moneyInc);
                        owner.setExp(owner.getExp() + expInc);
                        owner = normalizeAndUpdateSprite(owner);
                        if (ownerAttributeChange.setChanged(owner)) {
                            WSMessageSender.addResponse(WSResponseEnum.SPRITE_ATTRIBUTE_CHANGE, ownerAttributeChange);
                        }
                    }
                }
                // 被攻击者死亡后带来的物品奖励
                List<VictoryItemRewardDo> itemRewards = victoryReward.getItemRewards();
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
                    WSMessageSender.addResponse(WSResponseEnum.ITEM_GAIN, new ItemGainVo(itemOwner, itemReward.getItemType(), cnt));
                }
            }
        }
    }

    /**
     * 使精灵下线
     * 精灵下线只会下线自己，不会下线自己的宠物
     */
    public void offline(String spriteId) {
        // 发送下线消息
        WSMessageSender.addResponse(WSResponseEnum.OFFLINE, new OfflineVo(List.of(spriteId)));
        SpriteBo s = onlineSpriteMap.get(spriteId);
        if (s == null) {
            return;
        }
        onlineSpriteMap.remove(s.getId());
        // 精灵下线，信息需要立即写入数据库
        if (s.getHp() != 0 || s.getType() == SpriteTypeEnum.USER) {
            spriteMapper.updateById(s);
        } else {
            // 删除精灵
            spriteMapper.deleteById(spriteId);
        }
    }

    /**
     * 修改精灵生命
     *
     * @return 精灵是否死亡
     */
    @Transactional
    public boolean modifyLife(String spriteId, int val) {
        SpriteDo sprite = selectById(spriteId);
        boolean dead = false;
        if (sprite == null) {
            throw new BusinessException(StatusCodeEnum.SPRITE_NOT_FOUND);
        }
        int originHp = sprite.getHp();
        // 扣除目标精灵生命
        sprite.setHp(sprite.getHp() + val);
        // 判断目标精灵是否死亡
        if (sprite.getHp() <= 0) {
            sprite.setHp(0);
            dead = true;
        }
        // 如果满血
        if (sprite.getHp() > MAX_HP) {
            sprite.setHp(MAX_HP);
        }
        HpChangeVo hpChangeVo = new HpChangeVo(spriteId, originHp, sprite.getHp() - originHp);
        if (hpChangeVo.getHpChange() != 0) {
            WSMessageSender.addResponse(WSResponseEnum.SPRITE_HP_CHANGE, hpChangeVo);
            // 更新目标精灵
            normalizeAndUpdateSprite(sprite);
        }
        return dead;
    }

    /**
     * 驯服&喂养精灵
     *
     * @param sourceSprite 驯服/喂养者
     * @param targetSprite 被驯服/喂养者
     * @return 驯服结果
     */
    @Transactional
    public FeedResultEnum feed(SpriteBo sourceSprite, SpriteBo targetSprite) {
        // 判断是否可驯服
        List<FeedDo> feedList = feedService.selectBySpriteType(targetSprite.getType());
        if (feedList.isEmpty()) {
            return FeedResultEnum.CANNOT_TAMED;
        }
        // 是否手持了驯服所需物品
        ItemBo handHeldItem = sourceSprite.getEquipments().stream()
                .filter(item -> item.getPosition() == ItemPositionEnum.HANDHELD)
                .findFirst()
                .orElse(null);
        if (handHeldItem == null) {
            return FeedResultEnum.NO_ITEM;
        }
        // 得到当前手持物品对应的驯服信息
        FeedDo feed = feedList.stream()
                .filter(f -> f.getItemType() == handHeldItem.getItemType())
                .findFirst()
                .orElse(null);
        if (feed == null) {
            return FeedResultEnum.NO_ITEM;
        }
        // 喂养目标精灵
        targetSprite.setHunger(targetSprite.getHunger() + feed.getHungerInc());
        targetSprite.setExp(targetSprite.getExp() + feed.getExpInc());
        // 从手持物品栏减少1个物品（如果是最后一个物品，则删除）
        handHeldItem.setItemCount(handHeldItem.getItemCount() - 1);
        itemService.updateItem(handHeldItem);
        // 驯服会消耗物品，因此使缓存失效
        invalidateSpriteCache(sourceSprite.getId());
        // 判断是否已经有主人
        if (targetSprite.getOwner() != null && !targetSprite.getOwner().equals(sourceSprite.getId())) {
            normalizeAndUpdateSprite(targetSprite);
            return FeedResultEnum.ALREADY_TAMED;
        }
        // 如果已经被自己驯服，那就直接返回喂养成功
        if (targetSprite.getOwner() != null) {
            normalizeAndUpdateSprite(targetSprite);
            WSMessageSender.addResponse(WSResponseEnum.FEED_RESULT, new FeedVo(
                    sourceSprite.getId(), targetSprite.getId(), FeedResultEnum.FEED_SUCCESS
            ));
            return FeedResultEnum.FEED_SUCCESS;
        }
        // 否则是野生的，以一定概率驯服宠物
        if (GameCache.random.nextDouble() < feed.getTameProb()) {
            // 驯服成功
            targetSprite.setOwner(sourceSprite.getId());
            normalizeAndUpdateSprite(targetSprite);
            WSMessageSender.addResponse(WSResponseEnum.FEED_RESULT, new FeedVo(
                    sourceSprite.getId(), targetSprite.getId(), FeedResultEnum.TAME_SUCCESS
            ));
            return FeedResultEnum.TAME_SUCCESS;
        } else {
            // 驯服失败
            normalizeAndUpdateSprite(targetSprite);
            WSMessageSender.addResponse(WSResponseEnum.FEED_RESULT, new FeedVo(
                    sourceSprite.getId(), targetSprite.getId(), FeedResultEnum.TAME_FAIL
            ));
            return FeedResultEnum.TAME_FAIL;
        }
    }

    @Transactional
    public void interact(SpriteBo sourceSprite, SpriteBo targetSprite) {
        // 先尝试驯服/喂养
        var feedResult = feed(sourceSprite, targetSprite);
        // 如果驯服结果是“已经有主人”或者“驯服成功”或者“驯服失败”或者“喂养成功”，说明本次交互的目的的确是驯服/喂养，而非攻击
        if (feedResult == FeedResultEnum.ALREADY_TAMED || feedResult == FeedResultEnum.TAME_SUCCESS
                || feedResult == FeedResultEnum.TAME_FAIL || feedResult == FeedResultEnum.FEED_SUCCESS) {
            return;
        }
        // 尝试给目标精灵使用物品
        ItemBo handItem = sourceSprite.getEquipments().stream()
                .filter(item -> item.getPosition() == ItemPositionEnum.HANDHELD)
                .findFirst()
                .orElse(null);
        if (handItem != null) {
            var result = itemService.useItem(targetSprite, handItem);
            if (result == UseItemResultEnum.ITEM_USE_SUCCESS) {
                return;
            }
        }
        // 否则本次交互的目的是进行攻击
        attack(sourceSprite, targetSprite);
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
        coordinate(sprite);
        return sprite;
    }

    /** 坐标通知 */
    private void coordinate(SpriteDo sprite) {
        WSMessageSender.addResponse(
                WSResponseEnum.MOVE,
                new MoveVo(sprite.getId(),
                        1,
                        DataCompressor.compressPath(List.of(
                                new Point(sprite.getX().intValue(), sprite.getY().intValue()),
                                new Point(sprite.getX().intValue(), sprite.getY().intValue()) // 移动至少要有两个点
                        )),
                        null, null, null
                )
        );
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

    @Nullable
    public MoveVo move(SpriteBo sprite) {
        var agent = spriteAgentMap.get(sprite.getType());
        if (agent == null) {
            return null;
        }
        MoveBo moveBo = agent.act(sprite);
        return mapService.move(sprite, moveBo, agent.mapBitsPermissions(sprite));
    }

    /**
     * 该接口定义了游戏中不同类型精灵行为的基础框架。
     * 每种精灵类型（如玩家、狗、蜘蛛等）都应该有一个对应的实现类，
     * 在这个实现类中具体定义了该类型精灵的行为逻辑。
     */
    private interface SpriteAgent {
        /**
         * act 方法是每个精灵行动逻辑的核心。
         * 它定义了当游戏循环每次迭代时，精灵应该执行的操作。
         *
         * @param sprite 需要执行操作的精灵
         * @return 精灵的移动操作
         */
        MoveBo act(SpriteBo sprite);

        /**
         * 用于获取精灵在地图上的移动权限
         */
        default MapBitsPermissionsBo mapBitsPermissions(SpriteBo sprite) {
            return MapBitsPermissionsBo.DEFAULT_MAP_BITS_PERMISSIONS;
        }

        /** 被攻击后的行为 */
        default void onAttacked(SpriteBo me, SpriteBo attacker) {
            me.setTargetSpriteId(attacker.getId());
        }
    }

    private class DogAgent implements SpriteAgent {
        @Override
        public MoveBo act(SpriteBo sprite) {
            // 如果狗有目标精灵（并以一定概率忘记目标），那么狗就会攻击目标精灵
            SpriteBo target = getValidTargetWithRandomForget(sprite, 0.2)
                    .map(s -> selectOnlineById(s.getId()))
                    .orElse(null);
            if (target != null) {
                return MoveBo.moveToSprite(target);
            }
            // 如果狗有主人
            SpriteBo owner = getValidOwner(sprite).orElse(null);
            if (owner != null) {
                // 一定概率就跟着主人走
                return MoveBo.moveToPoint(owner.getX(), owner.getY())
                        .keepDistance()
                        .moveWithProb(0.75);
            }
            // 一定概率随机移动
            return MoveBo.randomMove(sprite).moveWithProb(0.25);
        }
    }

    private class EarthboundSpiritAgent implements SpriteAgent {
        @Override
        public MoveBo act(SpriteBo sprite) {
            // 如果有目标
            SpriteBo target = getValidTargetWithRandomForget(sprite, 0.25)
                    .map(s -> selectOnlineById(s.getId())).orElse(null);
            if (target != null) {
                return MoveBo.moveToSprite(target).moveWithProb(0.85);
            }
            // 如果有其他视野范围内的地缚灵有目标，则同样以这个目标为目标
            target = mapService.findAllTargetsInSight(sprite, (s) ->
                            s.getType() == SpriteTypeEnum.EARTHBOUND_SPIRIT)
                    .stream()
                    .map(x -> getValidTarget(x))
                    .flatMap(Optional::stream) // 将每个 Optional 对象转换为一个可能为空的流，然后将这些流合并起来
                    .findAny()
                    .map(s -> selectOnlineById(s.getId()))
                    .orElse(null);
            if (target != null) {
                sprite.setTargetSpriteId(target.getId());
                return MoveBo.moveToSprite(target).moveWithProb(0.85);
            }
            // 否则随机移动
            return MoveBo.randomMove(sprite).moveWithProb(0.25);
        }

        /** 默认只能在墓碑周围移动 */
        private static final int DEFAULT_ALLOW = MapBitsPermissionsBo.mapBitArrayToInt(MapBitEnum.SURROUNDING_TOMBSTONE);

        /** 默认不能在希腊神庙周围移动 */
        private static final int DEFAULT_FORBID = MapBitsPermissionsBo.mapBitArrayToInt(MapBitEnum.SURROUNDING_GREEK_TEMPLE);

        @Override
        public MapBitsPermissionsBo mapBitsPermissions(SpriteBo sprite) {
            int obstacles = 0; // 幽灵可以穿过任何障碍物
            // 默认只能在墓碑周围移动
            int allow = DEFAULT_ALLOW;
            // 如果体力小于一定值，则允许在任意地方移动
            if (sprite.getHp() < 50) {
                allow = MapBitsPermissionsBo.DEFAULT_ALLOW;
            }
            // 默认不能在希腊神庙周围移动
            int forbid = DEFAULT_FORBID;
            // 如果等级大于一定值，则可以在希腊神庙周围移动
            if (sprite.getLevel() > 5) {
                forbid = MapBitsPermissionsBo.DEFAULT_FORBID;
            }
            return new MapBitsPermissionsBo(obstacles, allow, forbid);
        }
    }

    private class SpiderAgent implements SpriteAgent {
        @Override
        public MoveBo act(SpriteBo sprite) {
            // 在视觉范围内寻找一个目标
            // 蜘蛛的攻击目标需要满足的条件（必须有主人，并且不是蜘蛛）
            SpriteBo target = getValidTargetWithRandomForget(sprite, 0.15)
                    .map(s -> selectOnlineById(s.getId()))
                    .orElse(null);
            if (target == null) {
                target = mapService.findAnyTargetInSight(sprite,
                        (s) -> s.getType() != SpriteTypeEnum.SPIDER && (s.getOwner() != null || s.getType() == SpriteTypeEnum.USER)
                ).map(s -> selectOnlineById(s.getId())).orElse(null);
            }
            if (target == null) {
                // 随机移动
                return MoveBo.randomMove(sprite).moveWithProb(0.15);
            }
            sprite.setTargetSpriteId(target.getId());
            return MoveBo.moveToSprite(target);
        }

        /** 默认不能在希腊神庙周围移动 */
        private static final int DEFAULT_FORBID = MapBitsPermissionsBo.mapBitArrayToInt(MapBitEnum.SURROUNDING_GREEK_TEMPLE);

        @Override
        public MapBitsPermissionsBo mapBitsPermissions(SpriteBo sprite) {
            int obstacles = MapBitsPermissionsBo.DEFAULT_OBSTACLES;
            int allow = MapBitsPermissionsBo.DEFAULT_ALLOW;
            // 默认不能在希腊神庙周围移动
            int forbid = DEFAULT_FORBID;
            // 如果等级大于一定值，则可以在希腊神庙周围移动
            if (sprite.getLevel() > 5) {
                forbid = MapBitsPermissionsBo.DEFAULT_FORBID;
            }
            return new MapBitsPermissionsBo(obstacles, allow, forbid);
        }
    }

    private class UserAgent implements SpriteAgent {
        @Override
        public MoveBo act(SpriteBo sprite) {
            // 如果有精灵目标
            SpriteBo target = getValidTarget(sprite)
                    .map(s -> selectOnlineById(s.getId()))
                    .orElse(null);
            if (target != null) {
                sprite.setTargetSpriteId(null);
                return MoveBo.moveToSprite(target);
            }
            var x = sprite.getTargetX();
            var y = sprite.getTargetY();
            // 如果有建筑目标
            String targetBuildingId = sprite.getTargetBuildingId();
            if (targetBuildingId != null) {
                assert x != null;
                assert y != null;
                sprite.setTargetBuildingId(null);
                sprite.setTargetX(null);
                sprite.setTargetY(null);
                return MoveBo.moveToBuilding(targetBuildingId, x, y);
            }
            // 如果有目标点
            if (x != null && y != null) {
                sprite.setTargetX(null);
                sprite.setTargetY(null);
                return MoveBo.moveToPoint(x, y);
            }
            // 否则不移动
            return MoveBo.empty();
        }

        @Override
        public void onAttacked(SpriteBo me, SpriteBo attacker) {
            // 使自己的宠物把攻击者当作目标
            for (SpriteBo sprite : onlineSpriteMap.values()) {
                if (sprite.getOwner() != null && sprite.getOwner().equals(me.getId())) {
                    sprite.setTargetSpriteId(attacker.getId());
                }
            }
        }
    }

    /** 精灵类型到agent的映射 */
    private final Map<SpriteTypeEnum, SpriteAgent> spriteAgentMap = Map.of(
            SpriteTypeEnum.DOG, new DogAgent(),
            SpriteTypeEnum.EARTHBOUND_SPIRIT, new EarthboundSpiritAgent(),
            SpriteTypeEnum.SPIDER, new SpiderAgent(),
            SpriteTypeEnum.USER, new UserAgent()
    );

    /** 精灵是否含有某效果 */
    public boolean hasEffect(String spriteId, EffectEnum effect) {
        SpriteBo sprite = selectOnlineById(spriteId);
        if (sprite == null) {
            return false;
        }
        return sprite.getEffects().stream()
                .anyMatch(e -> e.getEffect() == effect);
    }

    /** 无效化精灵的缓存 */
    public void invalidateSpriteCache(String spriteId) {
        SpriteBo sprite = onlineSpriteMap.get(spriteId);
        if (sprite != null) {
            sprite.setDirty(true);
        }
        WSMessageSender.addResponse(WSResponseEnum.SPRITE_CACHE_INVALIDATE, new SpriteCacheInvalidateVo(spriteId));
    }
}
