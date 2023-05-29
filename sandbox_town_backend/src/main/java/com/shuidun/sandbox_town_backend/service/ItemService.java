package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Player;
import com.shuidun.sandbox_town_backend.bean.PlayerItem;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.ItemMapper;
import com.shuidun.sandbox_town_backend.mapper.PlayerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.shuidun.sandbox_town_backend.enumeration.Constants.EXP_PER_LEVEL;

@Slf4j
@Service
public class ItemService {

    private final ItemMapper itemMapper;

    private final PlayerMapper playerMapper;

    public ItemService(ItemMapper itemMapper, PlayerMapper playerMapper) {
        this.itemMapper = itemMapper;
        this.playerMapper = playerMapper;
    }

    public List<PlayerItem> list(String playerName) {
        return itemMapper.listByUsername(playerName);
    }

    @Transactional
    public Player use(String username, String itemId) {
        // 判断玩家是否拥有该物品
        PlayerItem playerItem = itemMapper.getByUsernameAndItemId(username, itemId);
        if (playerItem == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 判断物品是否可用
        if (!playerItem.isUsable()) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_USABLE);
        }
        // 得到用户原先属性
        Player player = playerMapper.getPlayerByUsername(username);
        // 更新用户属性
        player.setMoney(player.getMoney() + playerItem.getMoneyInc());
        player.setExp(player.getExp() + playerItem.getExpInc());
        player.setHunger(player.getHunger() + playerItem.getHungerInc());
        player.setHp(player.getHp() + playerItem.getHpInc());
        player.setAttack(player.getAttack() + playerItem.getAttackInc());
        player.setDefense(player.getDefense() + playerItem.getDefenseInc());
        player.setSpeed(player.getSpeed() + playerItem.getSpeedInc());
        // 玩家是否升级
        if (player.getExp() >= EXP_PER_LEVEL) {
            player.setLevel(player.getLevel() + 1);
            player.setExp(player.getExp() - EXP_PER_LEVEL);
        }
        // 更新数据库
        playerMapper.updatePlayer(player);
        return player;
    }

}
