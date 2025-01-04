package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.FeedDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.mapper.FeedMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FeedService {
    private final FeedMapper feedMapper;

    public FeedService(FeedMapper feedMapper) {
        this.feedMapper = feedMapper;
    }

    @Cacheable(value = "feed::selectBySpriteType")
    public List<FeedDo> selectBySpriteType(SpriteTypeEnum type) {
        return feedMapper.selectBySpriteType(type);
    }
}
