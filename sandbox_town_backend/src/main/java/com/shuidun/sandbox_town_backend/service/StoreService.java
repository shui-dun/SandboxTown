package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.StoreItem;
import com.shuidun.sandbox_town_backend.mapper.StoreItemMapper;
import org.springframework.stereotype.Service;

@Service
public class StoreService {

    private final StoreItemMapper storeItemMapper;

    public StoreService(StoreItemMapper storeItemMapper) {
        this.storeItemMapper = storeItemMapper;
    }

}
