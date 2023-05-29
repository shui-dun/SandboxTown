package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.PlayerItem;
import com.shuidun.sandbox_town_backend.mapper.ItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ItemService {

    private final ItemMapper itemMapper;

    public ItemService(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public List<PlayerItem> list(String playerName) {
        return itemMapper.listByUsername(playerName);
    }
}
