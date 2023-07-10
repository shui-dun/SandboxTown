package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.StoreItemTypeDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreItemTypeMapper {
    /** 根据商店名获取商店物品列表 */
    @Select("SELECT * FROM store_item_type WHERE store = #{store}")
    public List<StoreItemTypeDo> selectByStore(String store);

    /** 根据商店名和物品名获取商店物品 */
    @Select("SELECT * FROM store_item_type WHERE store = #{store} AND item_type = #{itemType}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    // 禁用缓存
    StoreItemTypeDo selectByStoreAndItemType(String store, ItemTypeEnum itemType);

    /** 根据商店名和物品名更新商店物品所有信息 */
    @Select("UPDATE store_item_type SET count = #{count}, price = #{price} WHERE store = #{store} AND item_type = #{itemType}")
    void updateById(StoreItemTypeDo storeItemType);

    // 删除原有的商店商品
    @Select("DELETE FROM store_item_type WHERE store = #{store}")
    void deleteByStore(String store);

    // 插入新的商店商品
    @Select("INSERT INTO store_item_type VALUES (#{itemType}, #{store}, #{count}, #{price})")
    void insert(StoreItemTypeDo storeItemType);
}
