package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.StoreItem;
import com.shuidun.sandbox_town_backend.bean.StoreItemView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreItemMapper {
    /** 根据商店名获取商店物品列表 */
    @Select("SELECT * FROM store_item_view WHERE store = #{store}")
    public List<StoreItemView> listByStore(String store);

    /** 根据商店名和物品名获取商店物品 */
    @Select("SELECT * FROM store_item WHERE store = #{store} AND item = #{item}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE) // 禁用缓存
    StoreItem selectByStoreAndItem(String store, String item);

    /** 根据商店名和物品名更新商店物品所有信息 */
    @Select("UPDATE store_item SET count = #{count}, price = #{price} WHERE store = #{store} AND item = #{item}")
    void updateByStoreAndItem(StoreItem storeItem);

    // 删除原有的商店商品
    @Select("DELETE FROM store_item WHERE store = #{store}")
    void deleteAllByStore(String store);

    // 插入新的商店商品
    @Select("INSERT INTO store_item VALUES (#{item}, #{store}, #{count}, #{price})")
    void insert(StoreItem storeItem);
}
