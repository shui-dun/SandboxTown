package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.MyAndMyPetInfoVo;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.bean.SpriteDetailBo;
import com.shuidun.sandbox_town_backend.bean.SpriteDo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public RestResponseVo<SpriteDetailBo> getSpriteById(@PathVariable("id") String id) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                spriteService.selectByIdWithDetail(id));
    }

    @Operation(summary = "获取当前登陆玩家的详细信息")
    @GetMapping("/listMine")
    public RestResponseVo<SpriteDetailBo> getMyPlayerInfo() {
        String username = StpUtil.getLoginIdAsString();
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                spriteService.selectByIdWithDetail(username));
    }

    @Operation(summary = "获取整个地图上的所有角色信息")
    @GetMapping("/listAll")
    public RestResponseVo<List<SpriteDo>> getAllSprite() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                spriteService.getSpritesByMap(mapId));
    }

    @GetMapping("/listAllOnline")
    public RestResponseVo<List<SpriteDo>> getAllOnlineSprite() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                spriteService.getOnlineSprites());
    }

    @GetMapping("myAndMyPetInfo")
    public RestResponseVo<MyAndMyPetInfoVo> getMyAndMyPetInfo() {
        String username = StpUtil.getLoginIdAsString();
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                spriteService.getMyAndMyPetInfo(username));
    }
}
