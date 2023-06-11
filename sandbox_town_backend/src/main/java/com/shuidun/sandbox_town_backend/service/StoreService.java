package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Sprite;
import com.shuidun.sandbox_town_backend.bean.SpriteItem;
import com.shuidun.sandbox_town_backend.bean.StoreItem;
import com.shuidun.sandbox_town_backend.bean.StoreItemView;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.SpriteItemMapper;
import com.shuidun.sandbox_town_backend.mapper.SpriteMapper;
import com.shuidun.sandbox_town_backend.mapper.StoreItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreService {

    private final StoreItemMapper storeItemMapper;

    private final SpriteMapper spriteMapper;

    private final SpriteItemMapper spriteItemMapper;

    public StoreService(StoreItemMapper storeItemMapper, SpriteMapper spriteMapper, SpriteItemMapper spriteItemMapper) {
        this.storeItemMapper = storeItemMapper;
        this.spriteMapper = spriteMapper;
        this.spriteItemMapper = spriteItemMapper;
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
}
