package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.ApplePicking;
import com.shuidun.sandbox_town_backend.bean.Tree;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface TreeMapper {

    @Select("select * from tree where id = #{treeId}")
    Tree getTreeById(String treeId);

    @Insert("insert into apple_picking (sprite, tree, count, pick_time) values (#{sprite}, #{tree}, #{count}, #{pickTime})")
    void insertApplePicking(ApplePicking applePicking);

    @Select("select * from apple_picking where sprite = #{spriteId} and tree = #{treeId}")
    ApplePicking getApplePickingBySpriteIdAndTreeId(String spriteId, String treeId);

    @Update("update apple_picking set count = #{count}, pick_time = #{pickTime} where sprite = #{sprite} and tree = #{tree}")
    void updateApplePicking(ApplePicking applePicking);

    @Insert("insert into tree (id, apples_count, max_apples_count, limit_per_sprite) values (#{id}, #{applesCount}, #{maxApplesCount}, #{limitPerSprite})")
    void createTree(Tree tree);

    @Update("update tree set apples_count = #{applesCount}, max_apples_count = #{maxApplesCount}, limit_per_sprite = #{limitPerSprite} where id = #{id}")
    void updateTree(Tree tree);

    @Select("select * from tree")
    List<Tree> getAllTrees();
}
