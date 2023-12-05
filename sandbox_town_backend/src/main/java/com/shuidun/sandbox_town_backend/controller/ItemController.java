package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.ItemService;
import com.shuidun.sandbox_town_backend.service.SpriteService;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@RestController
@RequestMapping("/item")
@Slf4j
public class ItemController {

    private final ItemService itemService;

    private final SpriteService spriteService;

    public ItemController(ItemService itemService, SpriteService spriteService) {
        this.itemService = itemService;
        this.spriteService = spriteService;
    }

    @ApiOperation(value = "获取当前登陆玩家的所有物品信息")
    @GetMapping("/listMyItems")
    public RestResponseVo<List<ItemWithTypeAndLabelsBo>> listMyItems() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                itemService.listByOwnerWithTypeAndLabel(StpUtil.getLoginIdAsString()));
    }

    @ApiOperation(value = "获取当前登陆玩家的背包中的所有物品信息")
    @GetMapping("/listMyItemsInBackpack")
    public RestResponseVo<List<ItemWithTypeAndLabelsBo>> listMyItemsInBackpack() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                itemService.listItemsInBackpackByOwner(StpUtil.getLoginIdAsString()));
    }

    @ApiOperation(value = "获取当前登陆玩家的装备栏中的所有物品信息")
    @GetMapping("/listMyItemsInEquipment")
    public RestResponseVo<List<ItemWithTypeAndLabelsBo>> listMyItemsInEquipment() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                itemService.listItemsInEquipmentByOwner(StpUtil.getLoginIdAsString()));
    }

    @ApiOperation(value = "获取当前登陆玩家的物品栏中的所有物品信息")
    @GetMapping("/listMyItemsInItemBar")
    public RestResponseVo<List<ItemWithTypeAndLabelsBo>> listMyItemsInItemBar() {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                itemService.listItemsInItemBarByOwner(StpUtil.getLoginIdAsString()));
    }


    @ApiOperation(value = "使用物品")
    @PostMapping("/use")
    public RestResponseVo<Void> use(@NotNull String itemId) {
        // 之所以这里要以websocket而非http的方式发送消息，
        // 是因为http的方式发送消息，只能发送给当前请求的用户，
        // 而websocket的方式发送消息，可以发送给需要该消息的所有用户
        WSMessageSender.addResponses(spriteService.useItem(StpUtil.getLoginIdAsString(), itemId));
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "显示某个物品的详细信息")
    @GetMapping("/itemDetail")
    public RestResponseVo<ItemDetailBo> detail(@NotNull String itemId) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                itemService.getItemDetailById(itemId));
    }

    @ApiOperation(value = "显示某个物品类型的详细信息")
    @GetMapping("/itemTypeDetail")
    public RestResponseVo<ItemTypeDetailBo> detailByItemType(@NotNull ItemTypeEnum itemType) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                itemService.getItemTypeDetailById(itemType));
    }

    @ApiOperation(value = "显示某个物品类型的简略信息")
    @GetMapping("/itemTypeBrief")
    public RestResponseVo<ItemTypeDo> briefByItemType(@NotNull ItemTypeEnum itemType) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                itemService.getItemTypeBriefById(itemType));
    }

    @ApiOperation(value = "手持物品")
    @PostMapping("/hold")
    public RestResponseVo<Void> hold(@NotNull String itemId) {
        WSMessageSender.addResponses(itemService.hold(StpUtil.getLoginIdAsString(), itemId));
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "放入物品栏")
    @PostMapping("/putInItemBar")
    public RestResponseVo<Void> putInItemBar(@NotNull String itemId) {
        WSMessageSender.addResponses(itemService.putInItemBar(StpUtil.getLoginIdAsString(), itemId));
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "装备物品")
    @PostMapping("/equip")
    public RestResponseVo<Void> equip(@NotNull String itemId) {
        WSMessageSender.addResponses(itemService.equip(StpUtil.getLoginIdAsString(), itemId));
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "放入背包")
    @PostMapping("/putInBackpack")
    public RestResponseVo<Void> putInBackpack(@NotNull String itemId) {
        WSMessageSender.addResponses(itemService.putInBackpack(StpUtil.getLoginIdAsString(), itemId));
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }
}
