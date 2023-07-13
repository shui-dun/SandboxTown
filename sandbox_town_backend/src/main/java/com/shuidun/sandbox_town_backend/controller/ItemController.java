package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.ItemService;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
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
    public RestResponseVo<?> listMyItems() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, itemService.listByOwnerWithTypeAndLabel(StpUtil.getLoginIdAsString()));
    }

    /** 获取当前登陆玩家的背包中的所有物品信息 */
    @GetMapping("/listMyItemsInBackpack")
    public RestResponseVo<?> listMyItemsInBackpack() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, itemService.listItemsInBackpackByOwner(StpUtil.getLoginIdAsString()));
    }

    /** 获取当前登陆玩家的装备栏中的所有物品信息 */
    @GetMapping("/listMyItemsInEquipment")
    public RestResponseVo<?> listMyItemsInEquipment() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, itemService.listItemsInEquipmentByOwner(StpUtil.getLoginIdAsString()));
    }

    /** 获取当前登陆玩家的物品栏中的所有物品信息 */
    @GetMapping("/listMyItemsInItemBar")
    public RestResponseVo<?> listMyItemsInItemBar() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, itemService.listItemsInItemBarByOwner(StpUtil.getLoginIdAsString()));
    }


    /** 使用物品 */
    @PostMapping("/use")
    public RestResponseVo<?> use(String itemId) {
        // 之所以这里要以websocket而非http的方式发送消息，
        // 是因为http的方式发送消息，只能发送给当前请求的用户，
        // 而websocket的方式发送消息，可以发送给需要该消息的所有用户
        WSMessageSender.sendResponseList(itemService.use(StpUtil.getLoginIdAsString(), itemId));
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
    }

    /** 显示某个物品的详细信息 */
    @GetMapping("/itemDetail")
    public RestResponseVo<?> detail(String itemId) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, itemService.getItemDetailById(itemId));
    }

    /** 显示某个物品类型的详细信息 */
    @GetMapping("/itemTypeDetail")
    public RestResponseVo<?> detailByItemType(ItemTypeEnum itemType) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, itemService.getItemTypeDetailById(itemType));
    }

    /**
     * 手持物品
     */
    @PostMapping("/hold")
    public RestResponseVo<?> hold(String itemId) {
        WSMessageSender.sendResponseList(itemService.hold(StpUtil.getLoginIdAsString(), itemId));
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
    }
}
