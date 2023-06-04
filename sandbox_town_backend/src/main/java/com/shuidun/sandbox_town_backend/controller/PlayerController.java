package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponse;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.CharacterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/player")
public class PlayerController {
    @Autowired
    private CharacterService characterService;

    /** 获取玩家属性信息 */
    @GetMapping("/list/{username}")
    public RestResponse<?> getPlayerByUsername(@PathVariable("username") String username) {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, characterService.getCharacterInfoByID(username));
    }

    /** 获取当前登陆玩家的属性信息 */
    @GetMapping("/listMine")
    public RestResponse<?> getMyPlayerInfo() {
        String username = StpUtil.getLoginIdAsString();
        return new RestResponse<>(StatusCodeEnum.SUCCESS, characterService.getCharacterInfoByID(username));
    }
}
