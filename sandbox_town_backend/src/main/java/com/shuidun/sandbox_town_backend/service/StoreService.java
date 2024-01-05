package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.*;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StoreService implements RefreshableBuilding {

    @Lazy
    @Autowired
    private StoreService self;

    private final StoreItemTypeMapper storeItemTypeMapper;

    private final SpriteMapper spriteMapper;
    private final BuildingMapper buildingMapper;

    private final ItemTypeMapper itemTypeMapper;

    private final ItemService itemService;

    private final ItemMapper itemMapper;

    private final SpriteService spriteService;

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${mapId}")
    private String mapId;

    public StoreService(StoreItemTypeMapper storeItemTypeMapper, SpriteMapper spriteMapper, BuildingMapper buildingMapper, ItemTypeMapper itemTypeMapper, ItemService itemService, ItemMapper itemMapper, SpriteService spriteService, RedisTemplate<String, Object> redisTemplate) {
        this.storeItemTypeMapper = storeItemTypeMapper;
        this.spriteMapper = spriteMapper;
        this.buildingMapper = buildingMapper;
        this.itemTypeMapper = itemTypeMapper;
        this.itemService = itemService;
        this.itemMapper = itemMapper;
        this.spriteService = spriteService;
        this.redisTemplate = redisTemplate;
    }

    /** 列出商店的所有商品（带有标签信息） */
    // TODO: 未来应该分页
    @Cacheable(value = "store::listByStore", key = "#store")
    public List<StoreItemTypeWithTypeAndLabelsBo> listByStore(String store) {
        List<StoreItemTypeDo> storeItemTypes = storeItemTypeMapper.selectByStore(store);
        if (storeItemTypes.isEmpty()) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 得到所有的物品类型枚举
        List<ItemTypeEnum> itemTypes = storeItemTypes.stream().map(StoreItemTypeDo::getItemType).toList();
        // 得到所有的物品类型（带有标签）
        List<ItemTypeWithLabelsBo> itemTypeWithLabelsBo = itemService.listItemTypeWithLabels(itemTypes);
        Map<ItemTypeEnum, ItemTypeWithLabelsBo> itemTypeWithLabelsMap = itemTypeWithLabelsBo.stream().collect(Collectors.toMap(ItemTypeWithLabelsBo::getId, x -> x));
        // 为所有商店商品设置物品类型
        return storeItemTypes.stream().map(storeItemType -> {
            ItemTypeWithLabelsBo itemTypeWithLabels = itemTypeWithLabelsMap.get(storeItemType.getItemType());
            assert itemTypeWithLabels != null;
            return new StoreItemTypeWithTypeAndLabelsBo(storeItemType, itemTypeWithLabels);
        }).collect(Collectors.toList());
    }

    /** 买入商品 */
    @Transactional
    // 如果有多个CacheEvict，则使用@Caching注解
    @Caching(evict = {
            @CacheEvict(value = "store::listByStore", key = "#store"),
            @CacheEvict(value = "store::storeItemTypeDetail", key = "#store + '_' + #item")})
    public void buy(String spriteId, String store, ItemTypeEnum item, Integer amount) {
        StoreItemTypeDo storeItemType = storeItemTypeMapper.selectByStoreAndItemType(store, item);
        // 检查商品是否存在
        if (storeItemType == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 检查商品数量是否足够
        if (storeItemType.getCount() < amount) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_ENOUGH);
        }
        // 得到用户的金钱
        SpriteDo sprite = spriteService.selectById(spriteId);
        if (sprite == null) {
            throw new BusinessException(StatusCodeEnum.SPRITE_NOT_FOUND);
        }
        int money = sprite.getMoney();
        // 检查用户金钱是否足够
        if (money < storeItemType.getPrice() * amount) {
            throw new BusinessException(StatusCodeEnum.MONEY_NOT_ENOUGH);
        }
        // 更新用户金钱
        sprite.setMoney(money - storeItemType.getPrice() * amount);
        spriteMapper.updateById(sprite);
        // 更新商店商品数量
        storeItemType.setCount(storeItemType.getCount() - amount);
        storeItemTypeMapper.update(storeItemType);
        // 更新用户物品
        itemService.add(spriteId, item, amount);
    }


    /** 刷新商店商品 */
    @Transactional
    public void refresh(String store) {
        // 判断商店是否存在
        BuildingDo building = buildingMapper.selectById(store);
        if (building == null) {
            throw new BusinessException(StatusCodeEnum.BUILDING_NOT_FOUND);
        }
        // 删除原有的商店商品
        storeItemTypeMapper.deleteByStore(store);
        // 获取所有物品信息
        List<ItemTypeDo> itemTypes = itemTypeMapper.selectList(null);
        // 进货随机数目种类的商品
        int count = (int) (Math.random() * 6) + 3;
        if (count > itemTypes.size()) {
            count = itemTypes.size();
        }
        // 首先计算总稀有度
        int totalRarity = 0;
        for (ItemTypeDo itemType : itemTypes) {
            totalRarity += itemType.getRarity();
        }
        // 根据物品的稀有度，使用轮盘赌算法，随机选取物品
        for (int i = 0; i < count; i++) {
            int random = (int) (Math.random() * totalRarity);
            int sum = 0;
            for (ItemTypeDo itemType : itemTypes) {
                sum += itemType.getRarity();
                if (sum >= random) {
                    // 选中了该物品
                    // 以稀有度为基础，生成随机数量（稀有度越高，数量越多）
                    int itemCount = (int) (Math.random() * itemType.getRarity()) + 1;
                    // 如果该物品已经在商店中了，那么更新
                    // 这里要关闭缓存，否则会出现脏读
                    StoreItemTypeDo storeItemType = storeItemTypeMapper.selectByStoreAndItemType(store, itemType.getId());
                    if (storeItemType != null) {
                        storeItemType.setCount(storeItemType.getCount() + itemCount);
                        storeItemTypeMapper.update(storeItemType);
                    } else {
                        // 否则插入
                        // 以物品基础价格为基础，生成随机价格
                        int price = (int) (Math.random() * itemType.getBasicPrice()) + itemType.getBasicPrice() / 2;
                        // 生成商店商品
                        storeItemType = new StoreItemTypeDo(
                                itemType.getId(),
                                store,
                                itemCount,
                                price
                        );
                        storeItemTypeMapper.insert(storeItemType);
                    }
                    break;
                }
            }
        }
        // 删除缓存
        redisTemplate.delete("store::listByStore::" + store);
        Set<String> keys = redisTemplate.keys("store::storeItemTypeDetail::%s*".formatted(store));
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    /** 刷新所有商店商品 */
    @Override
    public void refreshAll() {
        // 得到所有商店
        List<BuildingDo> stores = buildingMapper.selectByMapIdAndType(mapId, BuildingTypeEnum.STORE);
        for (BuildingDo store : stores) {
            self.refresh(store.getId());
        }
    }

    /** 列出指定商店中指定商品的信息（包含标签信息、属性增益信息、效果信息等） */
    @Cacheable(value = "store::storeItemTypeDetail", key = "#store + '_' + #itemType")
    public StoreItemTypeDetailBo detailByStoreAndItemType(String store, ItemTypeEnum itemType) {
        StoreItemTypeDo storeItemType = storeItemTypeMapper.selectByStoreAndItemType(store, itemType);
        if (storeItemType == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 得到标签信息、属性增益信息、效果信息等
        return new StoreItemTypeDetailBo(storeItemType, itemService.getItemTypeDetailById(itemType));
    }

    public Integer soldPrice(String store, String itemId) {
        int price;
        // 得到物品信息（带有类型信息）
        ItemWithTypeBo item = itemService.getItemWithTypeById(itemId);
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 得到商店商品信息
        StoreItemTypeDo storeItemType = storeItemTypeMapper.selectByStoreAndItemType(store, item.getItemType());
        // 如果商店里面没有这个商品，那么那直接用物品的基础价格的一半
        if (storeItemType == null) {
            price = item.getItemTypeObj().getBasicPrice() / 2;
        } else {
            // 否则，用商店商品的价格的一半
            price = storeItemType.getPrice() / 2;
        }
        // 乘以寿命比例
        price = (int) ((double) price * item.getLife() / Constants.MAX_ITEM_LIFE);
        // 价格最低为1
        if (price <= 0) {
            price = 1;
        }
        return price;
    }

    @Transactional
    public void sell(String spriteId, String store, String itemId, Integer amount, Integer perPrice) {
        // 得到物品信息
        ItemDo item = itemMapper.selectById(itemId);
        // 检查物品是否存在
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 检查物品数量是否足够
        if (item.getItemCount() < amount) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_ENOUGH);
        }
        // 得到用户信息
        SpriteDo sprite = spriteService.selectById(spriteId);
        if (sprite == null) {
            throw new BusinessException(StatusCodeEnum.SPRITE_NOT_FOUND);
        }
        // 检查物品所有者
        if (!item.getOwner().equals(spriteId)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        // 判断价格是否正确
        if (!Objects.equals(perPrice, soldPrice(store, itemId))) {
            throw new BusinessException(StatusCodeEnum.PRICE_NOT_MATCH);
        }
        // 更新用户金钱
        sprite.setMoney(sprite.getMoney() + perPrice * amount);
        spriteService.normalizeAndUpdateSprite(sprite);
        // 更新物品数量
        itemService.reduce(spriteId, itemId, amount);
        // 更新商店商品数量（只有全新物品商店才会再次出售）
        if (item.getLife() == Constants.MAX_ITEM_LIFE) {
            StoreItemTypeDo storeItemType = storeItemTypeMapper.selectByStoreAndItemType(store, item.getItemType());
            if (storeItemType == null) {
                // 如果商店里面没有这个商品，那么那直接用物品的售卖价格的两倍
                storeItemType = new StoreItemTypeDo(
                        item.getItemType(),
                        store,
                        amount,
                        perPrice * 2
                );
                storeItemTypeMapper.insert(storeItemType);
            } else {
                // 否则，更新商店商品的数量
                storeItemType.setCount(storeItemType.getCount() + amount);
                storeItemTypeMapper.update(storeItemType);
            }
        }
        // 删除缓存
        redisTemplate.delete("store::listByStore::" + store);
        redisTemplate.delete("store::storeItemTypeDetail::%s_%s".formatted(store, item.getItemType()));
    }
}
