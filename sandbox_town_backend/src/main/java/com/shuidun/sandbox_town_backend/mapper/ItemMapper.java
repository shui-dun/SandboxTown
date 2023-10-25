package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.ItemDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemPositionEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ItemMapper extends BaseMapper<ItemDo> {

    /** 根据角色id获得其物品信息 */
    @Select("SELECT * FROM item WHERE owner = #{owner}")
    List<ItemDo> selectByOwner(String owner);

    /** 根据角色id和位置获得其物品信息 */
    @Select("SELECT * FROM item WHERE owner = #{owner} AND position = #{position}")
    List<ItemDo> selectByOwnerAndPosition(String owner, ItemPositionEnum position);

    /** 根据角色id和物品类型获得其物品信息 */
    @Select("SELECT * FROM item WHERE owner = #{owner} AND item_type = #{itemType}")
    List<ItemDo> selectByOwnerAndItemType(String owner, ItemTypeEnum itemType);

    /**
     * 根据角色id和位置列表获得其物品信息
     * 即，物品的位置在列表中的任意一个即可
     */
    @Select("""
            <script>
                SELECT * FROM item WHERE owner = #{owner} AND position in
                <foreach collection="positions" item="item" open="(" separator="," close=")">
                    #{item}
                </foreach>
            </script>
            """)
    List<ItemDo> selectByOwnerAndPositions(String owner, List<ItemPositionEnum> positions);
}
