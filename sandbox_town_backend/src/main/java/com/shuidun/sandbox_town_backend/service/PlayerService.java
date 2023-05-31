package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Player;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.PlayerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.shuidun.sandbox_town_backend.enumeration.Constants.EXP_PER_LEVEL;

@Service
public class PlayerService {
    private final PlayerMapper playerMapper;

    public PlayerService(PlayerMapper playerMapper) {
        this.playerMapper = playerMapper;
    }

    public Player getPlayerInfoByUsername(String username) {
        Player player = playerMapper.getPlayerByUsername(username);
        if (player == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        return player;
    }

    /**
     * 更新玩家属性
     *
     * @param username  玩家用户名
     * @param attribute 属性名
     * @param value     属性值
     * @return 更新后的玩家信息
     */
    @Transactional
    public Player updatePlayerAttribute(String username, String attribute, int value) {
        try {
            playerMapper.updatePlayerAttribute(username, attribute, value);
        } catch (BadSqlGrammarException e) {
            throw new BusinessException(StatusCodeEnum.ILLEGAL_ARGUMENT);
        }
        return getPlayerInfoByUsername(username);
    }

    /** 判断用户属性值是否在合理范围内（包含升级操作） */
    @Transactional
    public Player normalizeAndUpdatePlayer(Player player) {
        // 如果经验值足够升级，则升级
        if (player.getExp() >= EXP_PER_LEVEL) {
            player.setLevel(player.getLevel() + 1);
            player.setExp(player.getExp() - EXP_PER_LEVEL);
            // 更新玩家属性
            player.setMoney(player.getMoney() + 15);
            player.setHunger(player.getHunger() + 10);
            player.setHp(player.getHp() + 10);
            player.setAttack(player.getAttack() + 2);
            player.setDefense(player.getDefense() + 2);
            player.setSpeed(player.getSpeed() + 2);
        }
        // 判断属性是否在合理范围内
        if (player.getHunger() > 100) {
            player.setHunger(100);
        }
        if (player.getHunger() < 0) {
            player.setHunger(0);
        }
        if (player.getHp() > 100) {
            player.setHp(100);
        }
        if (player.getHp() < 0) {
            // 不能设置为0，因为0代表死亡
            player.setHp(1);
        }
        if (player.getAttack() < 0) {
            player.setAttack(0);
        }
        if (player.getDefense() < 0) {
            player.setDefense(0);
        }
        if (player.getSpeed() < 0) {
            player.setSpeed(0);
        }
        playerMapper.updatePlayer(player);
        return player;
    }
}
