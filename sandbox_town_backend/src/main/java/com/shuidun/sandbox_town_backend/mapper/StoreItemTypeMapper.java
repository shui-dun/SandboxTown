package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.StoreItemTypeDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreItemTypeMapper extends BaseMapper<StoreItemTypeDo> {
    /** 根据商店名获取商店物品列表 */
    default List<StoreItemTypeDo> selectByStore(String store) {
        return selectList(new LambdaQueryWrapper<StoreItemTypeDo>()
                .eq(StoreItemTypeDo::getStore, store));
    }

    /**
     * 根据商店名和物品名获取商店物品
     * 并禁用缓存
     */
    @Select("SELECT * FROM store_item_type WHERE store = #{store} AND item_type = #{itemType}")
    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    StoreItemTypeDo selectByStoreAndItemType(String store, ItemTypeEnum itemType);

    /** 根据商店名和物品名更新商店物品所有信息 */
    default void update(StoreItemTypeDo storeItemType) {
        update(storeItemType, new LambdaQueryWrapper<StoreItemTypeDo>()
                .eq(StoreItemTypeDo::getStore, storeItemType.getStore())
                .eq(StoreItemTypeDo::getItemType, storeItemType.getItemType()));
    }

    /** 删除原有的商店商品 */
    default void deleteByStore(String store) {
        delete(new LambdaQueryWrapper<StoreItemTypeDo>()
                .eq(StoreItemTypeDo::getStore, store));
    }
}
