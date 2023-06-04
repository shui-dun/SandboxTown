package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.Character;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.CharacterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.shuidun.sandbox_town_backend.enumeration.Constants.EXP_PER_LEVEL;

@Slf4j
@Service
public class CharacterService {
    private final CharacterMapper characterMapper;

    public CharacterService(CharacterMapper characterMapper) {
        this.characterMapper = characterMapper;
    }

    public Character getCharacterInfoByID(String id) {
        Character character = characterMapper.getCharacterByID(id);
        if (character == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        return character;
    }

    /**
     * 更新角色属性
     *
     * @param id        玩家用户名
     * @param attribute 属性名
     * @param value     属性值
     * @return 更新后的玩家信息
     */
    @Transactional
    public Character updateCharacterAttribute(String id, String attribute, int value) {
        try {
            characterMapper.updateCharacterAttribute(id, attribute, value);
        } catch (BadSqlGrammarException e) {
            throw new BusinessException(StatusCodeEnum.ILLEGAL_ARGUMENT);
        }
        return getCharacterInfoByID(id);
    }

    /** 判断角色属性值是否在合理范围内（包含升级操作） */
    @Transactional
    public Character normalizeAndUpdatePlayer(Character character) {
        // 如果经验值足够升级，则升级
        if (character.getExp() >= EXP_PER_LEVEL) {
            character.setLevel(character.getLevel() + 1);
            character.setExp(character.getExp() - EXP_PER_LEVEL);
            // 更新玩家属性
            character.setMoney(character.getMoney() + 15);
            character.setHunger(character.getHunger() + 10);
            character.setHp(character.getHp() + 10);
            character.setAttack(character.getAttack() + 2);
            character.setDefense(character.getDefense() + 2);
            character.setSpeed(character.getSpeed() + 2);
        }
        // 判断属性是否在合理范围内
        if (character.getHunger() > 100) {
            character.setHunger(100);
        }
        if (character.getHunger() < 0) {
            character.setHunger(0);
        }
        if (character.getHp() > 100) {
            character.setHp(100);
        }
        if (character.getHp() < 0) {
            // 不能设置为0，因为0代表死亡
            character.setHp(1);
        }
        if (character.getAttack() < 0) {
            character.setAttack(0);
        }
        if (character.getDefense() < 0) {
            character.setDefense(0);
        }
        if (character.getSpeed() < 0) {
            character.setSpeed(0);
        }
        characterMapper.updateCharacter(character);
        return character;
    }

    // 得到某个地图上的所有角色
    public List<Character> getCharactersByMap(String map) {
        return characterMapper.getCharactersByMap(map);
    }
}
