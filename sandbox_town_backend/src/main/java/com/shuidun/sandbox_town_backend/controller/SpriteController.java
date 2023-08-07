package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
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

    /** 获取角色详细信息 */
    @GetMapping("/list/{id}")
    public RestResponseVo<?> getSpriteById(@PathVariable("id") String id) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, spriteService.selectByIdWithDetail(id));
    }

    /** 获取当前登陆玩家的详细信息 */
    @GetMapping("/listMine")
    public RestResponseVo<?> getMyPlayerInfo() {
        String username = StpUtil.getLoginIdAsString();
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, spriteService.selectByIdWithDetail(username));
    }

    /** 获取整个地图上的所有角色信息 */
    @GetMapping("/listAll")
    public RestResponseVo<?> getAllSprite() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, spriteService.getSpritesByMap(mapId));
    }

    @GetMapping("/listAllOnline")
    public RestResponseVo<?> getAllOnlineSprite() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, spriteService.getOnlineSprites());
    }

    @GetMapping("myAndMyPetInfo")
    public RestResponseVo<?> getMyAndMyPetInfo() {
        String username = StpUtil.getLoginIdAsString();
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, spriteService.getMyAndMyPetInfo(username));
    }
}
