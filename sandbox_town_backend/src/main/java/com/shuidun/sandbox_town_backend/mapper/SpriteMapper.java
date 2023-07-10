package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.SpriteDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SpriteMapper extends BaseMapper<SpriteDo> {
    // 根据角色id获取角色信息（包含sprite_type表中的角色描述信息）
    @Select("""
            SELECT * FROM sprite
            INNER JOIN sprite_type
            ON sprite.type = sprite_type.type
            WHERE id = #{id}
            """)
    SpriteDo selectByIdWithType(@Param("id") String id);


    @Update("UPDATE sprite SET ${attribute} = #{value} WHERE id = #{id}")
    void updateAttribute(@Param("id") String id, @Param("attribute") String attribute, @Param("value") int value);

    // 得到某个地图上的所有角色
    @Select("SELECT * FROM sprite WHERE map = #{map}")
    List<SpriteDo> selectByMapId(@Param("map") String map);

    @Select("SELECT * FROM sprite WHERE owner = #{owner}")
    List<SpriteDo> selectByOwner(String owner);

    // 得到没有主人的角色
    @Select("SELECT * FROM sprite where owner IS NULL and type != 'user'")
    List<SpriteDo> selectUnownedSprites();
}
