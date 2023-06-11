package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponse;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/sprite")
public class SpriteController {
    private final SpriteService spriteService;

    private final String mapId;

    public SpriteController(SpriteService spriteService, @Value("${mapId}") String mapId) {
        this.spriteService = spriteService;
        this.mapId = mapId;
    }

    /** 获取角色属性信息 */
    @GetMapping("/list/{id}")
    public RestResponse<?> getSpriteById(@PathVariable("id") String id) {
        log.info("getSpriteById: {}", id);
        return new RestResponse<>(StatusCodeEnum.SUCCESS, spriteService.getSpriteInfoByID(id));
    }

    /** 获取当前登陆玩家的属性信息 */
    @GetMapping("/listMine")
    public RestResponse<?> getMyPlayerInfo() {
        String username = StpUtil.getLoginIdAsString();
        return new RestResponse<>(StatusCodeEnum.SUCCESS, spriteService.getSpriteInfoByID(username));
    }

    /** 获取整个地图上的所有角色信息 */
    @GetMapping("/listAll")
    public RestResponse<?> getAllSprite() {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, spriteService.getSpritesByMap(mapId));
    }

}
