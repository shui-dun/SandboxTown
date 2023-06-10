package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.StoreItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;

public interface StoreMapper extends BaseMapper<StoreItem> {
    default void myInsert(StoreItem storeItem) {
        System.out.println("myInsert");
        System.out.println(storeItem);
        insert(storeItem);
    }

    @Select("""
            <script>
                select * from store_item where item = #{item}
                <if test="item == 'apple'">
                    and store = 'store_Pk86H7rTSm2XJdGoHFe-7A'
                </if>
                <if test="item == 'banana'">
                    and store = 'store_Pk86H7rTSm2XJdGoHFe-7A'
                </if>
            </script>
            """)
    StoreItem mySelect(String item);
}
