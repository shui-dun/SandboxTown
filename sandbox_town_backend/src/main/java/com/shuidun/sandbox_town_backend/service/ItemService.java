package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Character;
import com.shuidun.sandbox_town_backend.bean.CharacterItem;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.ItemMapper;
import com.shuidun.sandbox_town_backend.mapper.CharacterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ItemService {

    private final ItemMapper itemMapper;

    private final CharacterMapper characterMapper;

    private final CharacterService characterService;

    public ItemService(ItemMapper itemMapper, CharacterMapper characterMapper, CharacterService characterService) {
        this.itemMapper = itemMapper;
        this.characterMapper = characterMapper;
        this.characterService = characterService;
    }

    public List<CharacterItem> list(String playerName) {
        return itemMapper.listByOwnerId(playerName);
    }

    @Transactional
    public Character use(String username, String itemId) {
        // 判断玩家是否拥有该物品
        CharacterItem characterItem = itemMapper.getByOwnerIdAndItemId(username, itemId);
        if (characterItem == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断物品是否可用
        if (!characterItem.isUsable()) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_USABLE);
        }
        // 得到用户原先属性
        Character character = characterMapper.getCharacterById(username);
        // 更新用户属性
        character.setMoney(character.getMoney() + characterItem.getMoneyInc());
        character.setExp(character.getExp() + characterItem.getExpInc());
        character.setHunger(character.getHunger() + characterItem.getHungerInc());
        character.setHp(character.getHp() + characterItem.getHpInc());
        character.setAttack(character.getAttack() + characterItem.getAttackInc());
        character.setDefense(character.getDefense() + characterItem.getDefenseInc());
        character.setSpeed(character.getSpeed() + characterItem.getSpeedInc());
        // 判断新属性是否在合理范围内（包含升级操作），随后写入数据库
        character = characterService.normalizeAndUpdatePlayer(character);
        // 判断是否是最后一个物品
        if (characterItem.getItemCount() <= 1) {
            // 删除物品
            itemMapper.deleteByOwnerIdAndItemId(username, itemId);
        } else {
            // 更新物品数量
            itemMapper.updateCountByOwnerIdAndItemId(username, itemId, characterItem.getItemCount() - 1);
        }
        return character;
    }


}
