package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Player;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.PlayerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {
    @Autowired
    private PlayerMapper playerMapper;

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
}
