package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.SpriteDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SpriteMapper extends BaseMapper<SpriteDo> {

    /** 得到没有主人的角色 */
    default List<SpriteDo> selectUnownedSprites() {
        return selectList(new LambdaQueryWrapper<SpriteDo>()
                .isNull(SpriteDo::getOwner)
                .ne(SpriteDo::getType, SpriteTypeEnum.USER));
    }

    /** 更新坐标 */
    default void updatePosition(String id, double x, double y) {
        update(null, new LambdaUpdateWrapper<SpriteDo>()
                .eq(SpriteDo::getId, id)
                .set(SpriteDo::getX, x)
                .set(SpriteDo::getY, y));
    }

    /** 更新精灵体力 */
    @Update("UPDATE sprite SET hp = CASE WHEN hp + #{incVal} > #{maxHp} THEN #{maxHp} ELSE hp + #{incVal} END WHERE id = #{spriteId}")
    void addSpriteLife(String spriteId, int incVal, int maxHp);

    /** 根据精灵类型和地图id得到精灵数量 */
    default long countByTypeAndMap(SpriteTypeEnum type, String map) {
        return selectCount(new LambdaQueryWrapper<SpriteDo>()
                .eq(SpriteDo::getType, type)
                .eq(SpriteDo::getMap, map));
    }

    /** 根据精灵类型列表和地图id得到精灵 */
    default List<SpriteDo> selectByTypesAndMap(List<SpriteTypeEnum> types, String map) {
        return selectList(new LambdaQueryWrapper<SpriteDo>()
                .in(SpriteDo::getType, types)
                .eq(SpriteDo::getMap, map));
    }

}
