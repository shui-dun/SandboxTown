package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponse;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.CharacterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/character")
public class CharacterController {
    private final CharacterService characterService;

    private final String mapId;

    public CharacterController(CharacterService characterService, @Value("${mapId}") String mapId) {
        this.characterService = characterService;
        this.mapId = mapId;
    }

    /** 获取角色属性信息 */
    @GetMapping("/list/{id}")
    public RestResponse<?> getCharacterById(@PathVariable("id") String id) {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, characterService.getCharacterInfoByID(id));
    }

    /** 获取当前登陆玩家的属性信息 */
    @GetMapping("/listMine")
    public RestResponse<?> getMyPlayerInfo() {
        String username = StpUtil.getLoginIdAsString();
        return new RestResponse<>(StatusCodeEnum.SUCCESS, characterService.getCharacterInfoByID(username));
    }

    /** 获取整个地图上的所有角色信息 */
    @GetMapping("/listAll")
    public RestResponse<?> getAllCharacter() {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, characterService.getCharactersByMap(mapId));
    }

}
