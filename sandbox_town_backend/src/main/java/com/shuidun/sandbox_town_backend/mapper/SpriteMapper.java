package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.SpriteDo;
import com.shuidun.sandbox_town_backend.bean.SpriteWithTypeBo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SpriteMapper extends BaseMapper<SpriteDo> {
    /** 根据角色id获取角色信息（包含sprite_type表中的信息） */
    @Select("""
            SELECT * FROM sprite
            INNER JOIN sprite_type
            ON sprite.type = sprite_type.type
            WHERE id = #{id}
            """)
    @Nullable
    SpriteWithTypeBo selectByIdWithType(String id);

    /** 得到某个地图上的所有角色 */
    default List<SpriteDo> selectByMapId(String map) {
        return selectList(new LambdaQueryWrapper<SpriteDo>()
                .eq(SpriteDo::getMap, map));
    }

    default List<SpriteDo> selectByOwner(String owner) {
        return selectList(new LambdaQueryWrapper<SpriteDo>()
                .eq(SpriteDo::getOwner, owner));
    }

    /** 得到没有主人的角色 */
    default List<SpriteDo> selectUnownedSprites() {
        return selectList(new LambdaQueryWrapper<SpriteDo>()
                .isNull(SpriteDo::getOwner)
                .ne(SpriteDo::getType, SpriteTypeEnum.USER));
    }

    /**
     * 对精灵列表spriteIds中的每个精灵，减少val的饥饿值（如果原先饥饿值小于val则减到0）
     */
    @Update("""
            <script>
                UPDATE sprite
                SET hunger = 
                CASE
                    WHEN hunger - #{val} &lt; 0 THEN 0
                    ELSE hunger - #{val}
                END
                WHERE id IN
                <foreach collection="spriteIds" item="spriteId" open="(" separator="," close=")">
                    #{spriteId}
                </foreach>
            </script>
            """)
    void reduceSpritesHunger(Collection<String> spriteIds, int val);

    /**
     * 对精灵列表spriteIds中的每个精灵，如果其饥饿值大于等于minHunger，则增加incVal的生命值，如果更新后的生命值大于最大生命值，则生命值等于最大生命值
     */
    @Update("""
            <script>
                UPDATE sprite
                SET hp = 
                CASE
                    WHEN hunger &gt;= #{minHunger} THEN
                        CASE
                            WHEN hp + #{incVal} &gt; #{maxHp} THEN #{maxHp}
                            ELSE hp + #{incVal}
                        END
                    ELSE hp
                END
                WHERE id IN
                <foreach collection="spriteIds" item="spriteId" open="(" separator="," close=")">
                    #{spriteId}
                </foreach>
            </script>
            """)
    void recoverSpritesLife(Collection<String> spriteIds, int minHunger, int incVal, int maxHp);

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

    default void updateOwnerByOwner(String fromId, String toId) {
        update(null, new LambdaUpdateWrapper<SpriteDo>()
                .eq(SpriteDo::getOwner, fromId)
                .set(SpriteDo::getOwner, toId));
    }

    /** 如果精灵存在，则更新精灵，否则添加精灵 */
    @Insert("""
            INSERT INTO sprite
            VALUES (#{id}, #{type}, #{owner}, #{money}, #{exp}, #{level}, #{hunger}, #{hp}, #{attack}, #{defense}, #{speed}, #{visionRange}, #{attackRange}, #{X}, #{Y}, #{width}, #{height}, #{map})
            ON DUPLICATE KEY UPDATE
            type = #{type}, owner = #{owner}, money = #{money}, exp = #{exp}, level = #{level}, hunger = #{hunger}, hp = #{hp}, attack = #{attack}, defense = #{defense}, speed = #{speed}, vision_range = #{visionRange}, attack_range = #{attackRange}, x = #{X}, y = #{Y}, width = #{width}, height = #{height}, map = #{map}
            """)
    void insertOrUpdateById(SpriteDo sprite);
}
