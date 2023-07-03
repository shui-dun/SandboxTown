package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StoreService {

    private final StoreItemTypeMapper storeItemTypeMapper;

    private final SpriteMapper spriteMapper;
    private final BuildingMapper buildingMapper;

    private final ItemTypeMapper itemTypeMapper;

    private final ItemService itemService;

    private final ItemMapper itemMapper;

    private final SpriteService spriteService;

    @Value("${mapId}")
    private String mapId;

    public StoreService(StoreItemTypeMapper storeItemTypeMapper, SpriteMapper spriteMapper, BuildingMapper buildingMapper, ItemTypeMapper itemTypeMapper, ItemService itemService, ItemMapper itemMapper, SpriteService spriteService) {
        this.storeItemTypeMapper = storeItemTypeMapper;
        this.spriteMapper = spriteMapper;
        this.buildingMapper = buildingMapper;
        this.itemTypeMapper = itemTypeMapper;
        this.itemService = itemService;
        this.itemMapper = itemMapper;
        this.spriteService = spriteService;
    }

    /** 列出商店的所有商品 */
    public List<StoreItemType> listByStore(String store) {
        List<StoreItemType> storeItemTypes = storeItemTypeMapper.selectByStore(store);
        if (storeItemTypes == null || storeItemTypes.isEmpty()) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 得到所有的物品类型枚举（不重复）
        Set<ItemTypeEnum> itemTypes = storeItemTypes.stream().map(StoreItemType::getItemType).collect(Collectors.toSet());
        // 得到所有的物品类型
        Map<ItemTypeEnum, ItemType> itemTypeMap = itemTypeMapper.selectBatchIds(itemTypes).stream().collect(Collectors.toMap(ItemType::getId, itemType -> itemType));
        // 为所有物品类型设置标签
        itemService.setLabelsForItemTypes(itemTypeMap.values());
        // 为所有商店商品设置物品类型
        storeItemTypes.forEach(storeItemType -> storeItemType.setItemTypeObj(itemTypeMap.get(storeItemType.getItemType())));
        return storeItemTypes;
    }

    /** 买入商品 */
    @Transactional
    public void buy(String spriteId, String store, ItemTypeEnum item, Integer amount) {
        StoreItemType storeItemType = storeItemTypeMapper.selectByStoreAndItemType(store, item);
        // 检查商品是否存在
        if (storeItemType == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 检查商品数量是否足够
        if (storeItemType.getCount() < amount) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_ENOUGH);
        }
        // 得到用户的金钱
        Sprite sprite = spriteMapper.selectById(spriteId);
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
        storeItemTypeMapper.updateById(storeItemType);
        // 更新用户物品
        itemService.add(spriteId, item, amount);
    }


    /** 刷新商店商品 */
    @Transactional
    public void refresh(String store) {
        // 删除原有的商店商品
        storeItemTypeMapper.deleteByStore(store);
        // 获取所有物品信息
        List<ItemType> itemTypes = itemTypeMapper.selectList(null);
        // 进货随机数目种类的商品
        int count = (int) (Math.random() * 6) + 3;
        if (count > itemTypes.size()) {
            count = itemTypes.size();
        }
        // 首先计算总稀有度
        int totalRarity = 0;
        for (ItemType itemType : itemTypes) {
            totalRarity += itemType.getRarity();
        }
        // 根据物品的稀有度，使用轮盘赌算法，随机选取物品
        for (int i = 0; i < count; i++) {
            int random = (int) (Math.random() * totalRarity);
            int sum = 0;
            for (ItemType itemType : itemTypes) {
                sum += itemType.getRarity();
                if (sum >= random) {
                    // 选中了该物品
                    // 以稀有度为基础，生成随机数量（稀有度越高，数量越多）
                    int itemCount = (int) (Math.random() * itemType.getRarity()) + 1;
                    // 如果该物品已经在商店中了，那么更新
                    // 这里要关闭缓存，否则会出现脏读
                    StoreItemType storeItemType = storeItemTypeMapper.selectByStoreAndItemType(store, itemType.getId());
                    if (storeItemType != null) {
                        storeItemType.setCount(storeItemType.getCount() + itemCount);
                        storeItemTypeMapper.updateById(storeItemType);
                    } else {
                        // 否则插入
                        // 以物品基础价格为基础，生成随机价格
                        int price = (int) (Math.random() * itemType.getBasicPrice()) + itemType.getBasicPrice() / 2;
                        // 生成商店商品
                        storeItemType = new StoreItemType();
                        storeItemType.setStore(store);
                        storeItemType.setItemType(itemType.getId());
                        storeItemType.setCount(itemCount);
                        storeItemType.setPrice(price);
                        storeItemTypeMapper.insert(storeItemType);
                    }
                    break;
                }
            }
        }
    }

    /** 刷新所有商店商品 */
    public void refreshAll() {
        // 得到所有商店
        List<Building> stores = buildingMapper.selectByMapIdAndType(mapId, BuildingTypeEnum.STORE);
        for (Building store : stores) {
            refresh(store.getId());
        }
    }

    /** 列出指定商店中指定商品的信息（包含标签信息、属性增益信息、效果信息等） */
    public StoreItemType detailByStoreAndItemType(String store, ItemTypeEnum itemType) {
        StoreItemType storeItemType = storeItemTypeMapper.selectByStoreAndItemType(store, itemType);
        if (storeItemType == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 得到标签信息、属性增益信息、效果信息等
        storeItemType.setItemTypeObj(itemService.getItemTypeDetailById(itemType));
        return storeItemType;
    }

    public Integer soldPrice(String store, String itemId) {
        int price;
        // 得到物品信息（带有类型信息）
        Item item = itemService.getItemWithTypeById(itemId);
        // 得到商店商品信息
        StoreItemType storeItemType = storeItemTypeMapper.selectByStoreAndItemType(store, item.getItemType());
        // 如果商店里面没有这个商品，那么那直接用物品的基础价格的一半
        if (storeItemType == null) {
            price = item.getItemTypeObj().getBasicPrice() / 2;
        } else {
            // 否则，用商店商品的价格的一半
            price = storeItemType.getPrice() / 2;
        }
        // 乘以寿命比例
        price = (int) ((double) price * item.getLife() / 100);
        // 价格最低为1
        if (price <= 0) {
            price = 1;
        }
        return price;
    }

    @Transactional
    public void sell(String spriteId, String store, String itemId, Integer amount, Integer perPrice) {
        // 得到物品信息
        Item item = itemMapper.selectById(itemId);
        // 检查物品是否存在
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 检查物品数量是否足够
        if (item.getItemCount() < amount) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_ENOUGH);
        }
        // 检查物品所有者
        if (!item.getOwner().equals(spriteId)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        // 判断价格是否正确
        if (!Objects.equals(perPrice, soldPrice(store, itemId))) {
            throw new BusinessException(StatusCodeEnum.PRICE_NOT_MATCH);
        }
        // 得到用户信息
        Sprite sprite = spriteMapper.selectById(spriteId);
        // 更新用户金钱
        sprite.setMoney(sprite.getMoney() + perPrice * amount);
        sprite = spriteService.normalizeAndUpdatePlayer(sprite);
        // 更新物品数量
        itemService.reduce(spriteId, itemId, amount);
        // 更新商店商品数量（只有全新物品商店才会再次出售）
        if (item.getLife() == 100) {
            StoreItemType storeItemType = storeItemTypeMapper.selectByStoreAndItemType(store, item.getItemType());
            if (storeItemType == null) {
                // 如果商店里面没有这个商品，那么那直接用物品的售卖价格的两倍
                storeItemType = new StoreItemType();
                storeItemType.setStore(store);
                storeItemType.setItemType(item.getItemType());
                storeItemType.setCount(amount);
                storeItemType.setPrice(perPrice * 2);
                storeItemTypeMapper.insert(storeItemType);
            } else {
                // 否则，更新商店商品的数量
                storeItemType.setCount(storeItemType.getCount() + amount);
                storeItemTypeMapper.updateById(storeItemType);
            }
        }
    }
}
