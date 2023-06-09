package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.StoreItem;
import com.shuidun.sandbox_town_backend.mapper.StoreMapper;
import org.springframework.stereotype.Service;

@Service
public class StoreService {

    private final StoreMapper storeMapper;

    public StoreService(StoreMapper storeMapper) {
        this.storeMapper = storeMapper;
    }

    public void foo() {
        StoreItem storeItem = new StoreItem();
        storeItem.setItem("apple");
        storeItem.setStore("store_Pk86H7rTSm2XJdGoHFe-7A");
        storeItem.setCount(1);
        storeItem.setMaxCount(2);
        storeItem.setPrice(3);
        storeMapper.insert(storeItem);
    }
}
