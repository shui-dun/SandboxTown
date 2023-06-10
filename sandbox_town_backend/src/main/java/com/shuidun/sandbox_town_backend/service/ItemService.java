package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Sprite;
import com.shuidun.sandbox_town_backend.bean.SpriteItemView;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.SpriteItemMapper;
import com.shuidun.sandbox_town_backend.mapper.SpriteMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ItemService {

    private final SpriteItemMapper spriteItemMapper;

    private final SpriteMapper spriteMapper;

    private final SpriteService spriteService;

    public ItemService(SpriteItemMapper spriteItemMapper, SpriteMapper spriteMapper, SpriteService spriteService) {
        this.spriteItemMapper = spriteItemMapper;
        this.spriteMapper = spriteMapper;
        this.spriteService = spriteService;
    }

    public List<SpriteItemView> list(String playerName) {
        return spriteItemMapper.listByOwnerId(playerName);
    }

    @Transactional
    public Sprite use(String username, String itemId) {
        // 判断玩家是否拥有该物品
        SpriteItemView spriteItemView = spriteItemMapper.getByOwnerIdAndItemId(username, itemId);
        if (spriteItemView == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断物品是否可用
        if (!spriteItemView.getUsable()) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_USABLE);
        }
        // 得到用户原先属性
        Sprite sprite = spriteMapper.getSpriteById(username);
        // 更新用户属性
        sprite.setMoney(sprite.getMoney() + spriteItemView.getMoneyInc());
        sprite.setExp(sprite.getExp() + spriteItemView.getExpInc());
        sprite.setHunger(sprite.getHunger() + spriteItemView.getHungerInc());
        sprite.setHp(sprite.getHp() + spriteItemView.getHpInc());
        sprite.setAttack(sprite.getAttack() + spriteItemView.getAttackInc());
        sprite.setDefense(sprite.getDefense() + spriteItemView.getDefenseInc());
        sprite.setSpeed(sprite.getSpeed() + spriteItemView.getSpeedInc());
        // 判断新属性是否在合理范围内（包含升级操作），随后写入数据库
        sprite = spriteService.normalizeAndUpdatePlayer(sprite);
        // 判断是否是最后一个物品
        if (spriteItemView.getItemCount() <= 1) {
            // 删除物品
            spriteItemMapper.deleteByOwnerIdAndItemId(username, itemId);
        } else {
            // 更新物品数量
            spriteItemMapper.updateByOwnerIdAndItemId(username, itemId, spriteItemView.getItemCount() - 1);
        }
        return sprite;
    }

    // 给玩家添加物品
    @Transactional
    public void add(String username, String itemId, int count) {
        // 判断玩家是否拥有该物品
        SpriteItemView spriteItemView = spriteItemMapper.getByOwnerIdAndItemId(username, itemId);
        if (spriteItemView == null) {
            // 玩家没有该物品，直接插入
            spriteItemMapper.insert(username, itemId, count);
        } else {
            // 玩家有该物品，更新数量
            spriteItemMapper.updateByOwnerIdAndItemId(username, itemId, spriteItemView.getItemCount() + count);
        }

    }
}
