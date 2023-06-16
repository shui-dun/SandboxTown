package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class StoreService {

    private final StoreItemMapper storeItemMapper;

    private final SpriteMapper spriteMapper;

    private final SpriteItemMapper spriteItemMapper;

    private final BuildingMapper buildingMapper;

    private final ItemMapper itemMapper;

    private final String mapId;

    public StoreService(StoreItemMapper storeItemMapper, SpriteMapper spriteMapper, SpriteItemMapper spriteItemMapper, BuildingMapper buildingMapper, ItemMapper itemMapper, @Value("${mapId}") String mapId) {
        this.storeItemMapper = storeItemMapper;
        this.spriteMapper = spriteMapper;
        this.spriteItemMapper = spriteItemMapper;
        this.buildingMapper = buildingMapper;
        this.itemMapper = itemMapper;
        this.mapId = mapId;
    }

    public List<StoreItemView> listByStore(String store) {
        return storeItemMapper.listByStore(store);
    }

    /** 买入商品 */
    @Transactional
    public void buy(String username, String store, String item, Integer amount) {
        StoreItem storeItem = storeItemMapper.selectByStoreAndItem(store, item);
        // 检查商品是否存在
        if (storeItem == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 检查商品数量是否足够
        if (storeItem.getCount() < amount) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_ENOUGH);
        }
        // 得到用户的金钱
        Sprite userSprite = spriteMapper.selectById(username);
        int money = userSprite.getMoney();
        // 检查用户金钱是否足够
        if (money < storeItem.getPrice() * amount) {
            throw new BusinessException(StatusCodeEnum.MONEY_NOT_ENOUGH);
        }
        // 更新用户金钱
        userSprite.setMoney(money - storeItem.getPrice() * amount);
        spriteMapper.updateById(userSprite);
        // 更新商店商品数量
        storeItem.setCount(storeItem.getCount() - amount);
        storeItemMapper.updateByStoreAndItem(storeItem);
        // 更新用户物品
        SpriteItem spriteItem = spriteItemMapper.getSpriteItemByOwnerIdAndItemId(username, item);
        spriteItemMapper.updateByOwnerIdAndItemId(username, item, spriteItem.getItemCount() + amount);
    }


    /** 刷新商店商品 */
    @Transactional
    public void refresh(String store) {
        // 删除原有的商店商品
        storeItemMapper.deleteAllByStore(store);
        // 获取所有物品信息
        List<Item> items = itemMapper.selectList(null);
        // 进货随机数目种类的商品
        int count = (int) (Math.random() * 6) + 3;
        if (count > items.size()) {
            count = items.size();
        }
        // 首先计算总稀有度
        int totalRarity = 0;
        for (Item item : items) {
            totalRarity += item.getRarity();
        }
        // 根据物品的稀有度，使用轮盘赌算法，随机选取物品
        for (int i = 0; i < count; i++) {
            int random = (int) (Math.random() * totalRarity);
            int sum = 0;
            for (Item item : items) {
                sum += item.getRarity();
                if (sum >= random) {
                    // 选中了该物品
                    // 以稀有度为基础，生成随机数量（稀有度越高，数量越多）
                    int itemCount = (int) (Math.random() * item.getRarity()) + 1;
                    // 如果该物品已经在商店中了，那么更新
                    // 这里要关闭缓存，否则会出现脏读
                    StoreItem storeItem = storeItemMapper.selectByStoreAndItem(store, item.getId());
                    if (storeItem != null) {
                        storeItem.setCount(storeItem.getCount() + itemCount);
                        storeItemMapper.updateByStoreAndItem(storeItem);
                    } else {
                        // 否则插入
                        // 以物品基础价格为基础，生成随机价格
                        int price = (int) (Math.random() * item.getBasicPrice()) + item.getBasicPrice() / 2;
                        // 生成商店商品
                        storeItem = new StoreItem();
                        storeItem.setStore(store);
                        storeItem.setItem(item.getId());
                        storeItem.setCount(itemCount);
                        storeItem.setPrice(price);
                        storeItemMapper.insert(storeItem);
                    }
                    break;
                }
            }
        }
    }

    /** 刷新所有商店商品 */
    public void refreshAll() {
        // 得到所有商店
        List<Building> stores = buildingMapper.getBuildingsByMapIdAndType(mapId, "store");
        for (Building store : stores) {
            refresh(store.getId());
        }
    }
}
