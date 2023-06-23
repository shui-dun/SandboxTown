package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Item;
import com.shuidun.sandbox_town_backend.bean.ItemType;
import com.shuidun.sandbox_town_backend.bean.ItemTypeAttribute;
import com.shuidun.sandbox_town_backend.bean.Sprite;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.*;
import com.shuidun.sandbox_town_backend.utils.NameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ItemService {

    private final SpriteMapper spriteMapper;

    private final SpriteService spriteService;

    private final ItemMapper itemMapper;

    private final ItemTypeLabelMapper itemTypeLabelMapper;

    private final ItemTypeAttributeMapper itemTypeAttributeMapper;

    private final ItemTypeMapper itemTypeMapper;

    public ItemService(SpriteMapper spriteMapper, SpriteService spriteService, ItemMapper itemMapper, ItemTypeLabelMapper itemTypeLabelMapper, ItemTypeAttributeMapper itemTypeAttributeMapper, ItemTypeMapper itemTypeMapper) {
        this.spriteMapper = spriteMapper;
        this.spriteService = spriteService;
        this.itemMapper = itemMapper;
        this.itemTypeLabelMapper = itemTypeLabelMapper;
        this.itemTypeAttributeMapper = itemTypeAttributeMapper;
        this.itemTypeMapper = itemTypeMapper;
    }

    public List<Item> list(String owner) {
        return itemMapper.selectByOwner(owner);
    }

    @Transactional
    public Sprite use(String owner, String itemId) {
        // 判断物品是否存在
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断角色是否拥有该物品
        if (!item.getOwner().equals(owner)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        // 判断物品是否可用
        List<String> labels = itemTypeLabelMapper.selectByItemType(item.getItemType());
        if (!labels.contains("food") && !labels.contains("usable")) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_USABLE);
        }
        // 得到物品带来的属性变化
        ItemTypeAttribute itemTypeAttribute = itemTypeAttributeMapper.selectByItemTypeAndOperation(item.getItemType(), "use");
        // TODO: 根据物品等级计算属性变化
        // 得到角色原先属性
        Sprite sprite = spriteMapper.selectById(owner);
        // 更新角色属性
        sprite.setMoney(sprite.getMoney() + itemTypeAttribute.getMoneyInc());
        sprite.setExp(sprite.getExp() + itemTypeAttribute.getExpInc());
        sprite.setHunger(sprite.getHunger() + itemTypeAttribute.getHungerInc());
        sprite.setHp(sprite.getHp() + itemTypeAttribute.getHpInc());
        sprite.setAttack(sprite.getAttack() + itemTypeAttribute.getAttackInc());
        sprite.setDefense(sprite.getDefense() + itemTypeAttribute.getDefenseInc());
        sprite.setSpeed(sprite.getSpeed() + itemTypeAttribute.getSpeedInc());
        // TODO: 向角色施加效果
        // 判断新属性是否在合理范围内（包含升级操作），随后写入数据库
        sprite = spriteService.normalizeAndUpdatePlayer(sprite);
        // 判断是否是最后一个物品
        if (item.getItemCount() <= 1) {
            // 删除物品
            itemMapper.deleteById(item);
        } else {
            // 更新物品数量
            item.setItemCount(item.getItemCount() - 1);
            itemMapper.updateById(item);
        }
        return sprite;
    }

    // 给玩家添加物品
    @Transactional
    public void add(String spriteId, String itemTypeId, int count) {
        ItemType itemType = itemTypeMapper.selectById(itemTypeId);
        // 判断物品是否可堆叠
        if (itemType.getDurability() != -1) {
            // 不可堆叠，直接插入
            for (int i = 0; i < count; i++) {
                Item item = new Item();
                item.setId(NameGenerator.generateItemName(itemTypeId));
                item.setOwner(spriteId);
                item.setItemType(itemTypeId);
                item.setItemCount(1);
                item.setLife(100);
                item.setLevel(1);
                item.setPosition("backpack");
                itemMapper.insert(item);
            }
        } else {
            // 判断玩家是否拥有该物品
            List<Item> items = itemMapper.selectByOwnerAndItemType(spriteId, itemTypeId);
            if (items == null || items.size() == 0) {
                // 玩家没有该物品，直接插入
                Item item = new Item();
                item.setId(NameGenerator.generateItemName(itemTypeId));
                item.setOwner(spriteId);
                item.setItemType(itemTypeId);
                item.setItemCount(count);
                item.setLife(100);
                item.setLevel(1);
                item.setPosition("backpack");
                itemMapper.insert(item);
            } else {
                Item item = items.get(0);
                // 玩家有该物品，更新数量
                item.setItemCount(item.getItemCount() + count);
                itemMapper.updateById(item);
            }
        }
    }
}
