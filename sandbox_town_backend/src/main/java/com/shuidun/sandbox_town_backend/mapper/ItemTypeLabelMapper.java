package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.ItemTypeLabel;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

public interface ItemTypeLabelMapper {
    // 获得物品类型所对应的所有标签
    @Select("select label from item_type_label where item_type=#{itemType}")
    Set<String> selectByItemType(String itemType);

    @Select("""
            <script>
                select * from item_type_label where item_type in
                <foreach collection="itemTypes" item="item" open="(" separator="," close=")">
                    #{item}
                </foreach>
            </script>
            """)
    List<ItemTypeLabel> selectByItemTypes(List<String> itemTypes);
}
