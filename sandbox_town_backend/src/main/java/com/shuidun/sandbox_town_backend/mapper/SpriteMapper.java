package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.SpriteDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
    SpriteDo selectByIdWithType(@Param("id") String id);


    @Update("UPDATE sprite SET ${attribute} = #{value} WHERE id = #{id}")
    void updateAttribute(@Param("id") String id, @Param("attribute") String attribute, @Param("value") int value);

    /** 得到某个地图上的所有角色 */
    @Select("SELECT * FROM sprite WHERE map = #{map}")
    List<SpriteDo> selectByMapId(@Param("map") String map);

    @Select("SELECT * FROM sprite WHERE owner = #{owner}")
    List<SpriteDo> selectByOwner(String owner);

    /** 得到没有主人的角色 */
    @Select("SELECT * FROM sprite where owner IS NULL and type != 'user'")
    List<SpriteDo> selectUnownedSprites();

    /**
     * 对精灵列表spriteIds中的每个精灵，减少val的饥饿值（如果原先饥饿值小于val则减到0）
     * UPDATE sprite
     * SET hunger = CASE
     * WHEN hunger - val < 0 THEN 0
     * ELSE hunger - val
     * END
     * WHERE id IN (1, 2, 3);
     * <p>
     * 注意，在<script>标签中，<和>需要转义为&lt;和&gt;
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
     * 对精灵列表spriteIds中的每个精灵，如果其饥饿值大于等于minHunger，则增加incVal的生命值，如果更新后的生命值大于最大生命值（100），则生命值等于最大生命值
     */
    @Update("""
            <script>
                UPDATE sprite
                SET hp = 
                CASE
                    WHEN hunger &gt;= #{minHunger} THEN
                        CASE
                            WHEN hp + #{incVal} &gt; 100 THEN 100
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
    void recoverSpritesLife(Collection<String> spriteIds, int minHunger, int incVal);

    /** 更新坐标 */
    @Update("UPDATE sprite SET x = #{x}, y = #{y} WHERE id = #{id}")
    void updatePosition(@Param("id") String id, @Param("x") int x, @Param("y") int y);

    /** 更新精灵体力（最大为100） */
    @Update("UPDATE sprite SET hp = CASE WHEN hp + #{incVal} > 100 THEN 100 ELSE hp + #{incVal} END WHERE id = #{spriteId}")
    void addSpriteLife(String spriteId, int incVal);
}
