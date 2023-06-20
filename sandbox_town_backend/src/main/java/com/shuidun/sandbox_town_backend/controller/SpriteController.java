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

    @Value("${mapId}")
    private String mapId;

    public SpriteController(SpriteService spriteService) {
        this.spriteService = spriteService;
    }

    /** 获取角色属性信息 */
    @GetMapping("/list/{id}")
    public RestResponse<?> getSpriteById(@PathVariable("id") String id) {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, spriteService.selectById(id));
    }

    /** 获取当前登陆玩家的属性信息 */
    @GetMapping("/listMine")
    public RestResponse<?> getMyPlayerInfo() {
        String username = StpUtil.getLoginIdAsString();
        return new RestResponse<>(StatusCodeEnum.SUCCESS, spriteService.selectById(username));
    }

    /** 获取整个地图上的所有角色信息 */
    @GetMapping("/listAll")
    public RestResponse<?> getAllSprite() {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, spriteService.getSpritesByMap(mapId));
    }

    @GetMapping("/listAllOnline")
    public RestResponse<?> getAllOnlineSprite() {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, spriteService.getOnlineSprites());
    }

    @GetMapping("myAndMyPetInfo")
    public RestResponse<?> getMyAndMyPetInfo() {
        String username = StpUtil.getLoginIdAsString();
        return new RestResponse<>(StatusCodeEnum.SUCCESS, spriteService.getMyAndMyPetInfo(username));
    }
}
