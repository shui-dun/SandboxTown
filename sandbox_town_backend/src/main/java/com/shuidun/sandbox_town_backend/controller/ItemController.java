package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponse;
import com.shuidun.sandbox_town_backend.enumeration.ItemPositionEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
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

    /** 获取当前登陆玩家的所有物品信息 */
    @GetMapping("/listMyItems")
    public RestResponse<?> listMyItems() {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, itemService.listByOwnerWithTypeAndLabel(StpUtil.getLoginIdAsString()));
    }

    /** 获取当前登陆玩家的背包中的所有物品信息 */
    @GetMapping("/listMyItemsInBackpack")
    public RestResponse<?> listMyItemsInBackpack() {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, itemService.listItemsByOwnerAndPositionWithTypeAndLabel(StpUtil.getLoginIdAsString(), ItemPositionEnum.BACKPACK));
    }


    /** 使用物品 */
    @PostMapping("/use")
    public RestResponse<?> use(String itemId) {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, itemService.use(StpUtil.getLoginIdAsString(), itemId));
    }

    /** 显示某个物品的详细信息 */
    @GetMapping("/itemDetail")
    public RestResponse<?> detail(String itemId) {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, itemService.getItemDetailById(itemId));
    }

    /** 显示某个物品类型的详细信息 */
    @GetMapping("/itemTypeDetail")
    public RestResponse<?> detailByItemType(ItemTypeEnum itemType) {
        return new RestResponse<>(StatusCodeEnum.SUCCESS, itemService.getItemTypeDetailById(itemType));
    }
}
