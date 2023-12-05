package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.bean.StoreItemTypeDetailBo;
import com.shuidun.sandbox_town_backend.bean.StoreItemTypeWithTypeAndLabelsBo;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.StoreService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @ApiOperation(value = "得到某个商店的所有商品列表")
    @GetMapping("/listByStore")
    public RestResponseVo<List<StoreItemTypeWithTypeAndLabelsBo>> listByStore(@NotNull String store) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                storeService.listByStore(store));
    }

    @ApiOperation(value = "买入商品")
    @PostMapping("/buy")
    public RestResponseVo<Void> buy(@NotNull @ApiParam(value = "商店") String store,
                                    @NotNull @ApiParam(value = "物品类型") ItemTypeEnum item,
                                    @NotNull @ApiParam(value = "购买数量") Integer amount) {
        storeService.buy(StpUtil.getLoginIdAsString(), store, item, amount);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "得到某个商店的某个商品的详细信息")
    @GetMapping("/getByStoreAndItemType")
    public RestResponseVo<StoreItemTypeDetailBo> getByStoreAndItem(@NotNull String store, @NotNull ItemTypeEnum itemType) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                storeService.detailByStoreAndItemType(store, itemType));
    }

    @ApiOperation(value = "得到用户向商店出售时的售价")
    @GetMapping("/soldPrice")
    public RestResponseVo<Integer> soldPrice(@NotNull String store, @NotNull String itemId) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                storeService.soldPrice(store, itemId));
    }

    @ApiOperation(value = "出售物品")
    @PostMapping("/sell")
    public RestResponseVo<Void> sell(@NotNull String store, @NotNull String itemId, @NotNull Integer amount, @NotNull Integer perPrice) {
        storeService.sell(StpUtil.getLoginIdAsString(), store, itemId, amount, perPrice);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

}

