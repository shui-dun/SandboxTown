package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.bean.SpriteBo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Validated
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

    @Operation(summary = "获取角色详细信息")
    @GetMapping("/list/{id}")
    public RestResponseVo<SpriteBo> getSpriteById(@PathVariable("id") String id) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                spriteService.selectById(id));
    }

    @Operation(summary = "获取当前登陆玩家的详细信息")
    @GetMapping("/listMine")
    public RestResponseVo<SpriteBo> getMyPlayerInfo() {
        String username = StpUtil.getLoginIdAsString();
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                spriteService.selectById(username));
    }

    @GetMapping("/listAllOnline")
    public RestResponseVo<Collection<SpriteBo>> getAllOnlineSprite() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                spriteService.getOnlineSprites().values());
    }

    @PostMapping("/online")
    public RestResponseVo<SpriteBo> online() {
        String username = StpUtil.getLoginIdAsString();
        SpriteBo spriteBo = spriteService.online(username);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, spriteBo);
    }

    @SaCheckRole("ADMIN")
    @Operation(summary = "刷新所有精灵")
    @PostMapping("/refreshAll")
    public RestResponseVo<Void> refreshAll() {
        spriteService.refreshAllSprites();
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }
}
