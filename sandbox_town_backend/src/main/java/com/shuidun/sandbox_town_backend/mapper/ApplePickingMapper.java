package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.ApplePickingDo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ApplePickingMapper {
    @Insert("insert into apple_picking values (#{sprite}, #{tree}, #{count}, #{pickTime})")
    void insert(ApplePickingDo applePicking);

    @Select("select * from apple_picking where sprite = #{spriteId} and tree = #{treeId}")
    ApplePickingDo selectById(String spriteId, String treeId);

    @Update("update apple_picking set count = #{count}, pick_time = #{pickTime} where sprite = #{sprite} and tree = #{tree}")
    void updateById(ApplePickingDo applePicking);

}
