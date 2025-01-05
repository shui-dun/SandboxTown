package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.SpriteTypeDo;
import com.shuidun.sandbox_town_backend.enumeration.SpriteTypeEnum;
import com.shuidun.sandbox_town_backend.mapper.SpriteTypeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpriteTypeService {

    private final SpriteTypeMapper spriteTypeMapper;

    public SpriteTypeService(SpriteTypeMapper spriteTypeMapper) {
        this.spriteTypeMapper = spriteTypeMapper;
    }

    @Cacheable(value = "spriteType", key = "#type")
    public SpriteTypeDo selectById(SpriteTypeEnum type) {
        return spriteTypeMapper.selectById(type);
    }
}
