package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponse;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.ItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/item")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /** 获取当前登陆玩家的物品信息 */
    @GetMapping("/listMine")
    public RestResponse<?> listMine() {
        return new  RestResponse<>(StatusCodeEnum.SUCCESS, itemService.listByOwnerWithTypeAndLabel(StpUtil.getLoginIdAsString()));
    }

    /** 使用物品 */
    @PostMapping("/use")
    public RestResponse<?> use(String itemId) {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, itemService.use(StpUtil.getLoginIdAsString(), itemId));
    }

    /** 显示某个物品的详细信息 */
    @GetMapping("/detail")
    public RestResponse<?> detail(String itemId) {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, itemService.detail(itemId));
    }
}
