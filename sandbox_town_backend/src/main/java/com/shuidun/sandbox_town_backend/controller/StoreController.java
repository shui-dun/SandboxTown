package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.bean.StoreItemTypeBo;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "得到某个商店的所有商品列表")
    @GetMapping("/listByStore")
    public RestResponseVo<List<StoreItemTypeBo>> listByStore(@NotNull @RequestParam String store) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                storeService.listByStore(store));
    }

    @Operation(summary = "买入商品")
    @PostMapping("/buy")
    public RestResponseVo<Void> buy(@NotNull @RequestParam @Parameter(description = "商店")
                                    String store,
                                    @NotNull @RequestParam @Parameter(description = "物品类型")
                                    ItemTypeEnum item,
                                    @NotNull @RequestParam @Parameter(description = "购买数量")
                                    Integer amount) {
        storeService.buy(StpUtil.getLoginIdAsString(), store, item, amount);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @Operation(summary = "得到某个商店的某个商品的详细信息")
    @GetMapping("/getByStoreAndItemType")
    public RestResponseVo<StoreItemTypeBo> getByStoreAndItem(@NotNull @RequestParam String store,
                                                             @NotNull @RequestParam ItemTypeEnum itemType) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                storeService.getByStoreAndItemType(store, itemType));
    }

    @Operation(summary = "得到用户向商店出售时的售价")
    @GetMapping("/soldPrice")
    public RestResponseVo<Integer> soldPrice(@NotNull @RequestParam String store,
                                             @NotNull @RequestParam String itemId) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                storeService.soldPrice(store, itemId));
    }

    @Operation(summary = "出售物品")
    @PostMapping("/sell")
    public RestResponseVo<Void> sell(@NotNull @RequestParam String store,
                                     @NotNull @RequestParam String itemId,
                                     @NotNull @RequestParam Integer amount,
                                     @NotNull @RequestParam Integer perPrice) {
        storeService.sell(StpUtil.getLoginIdAsString(), store, itemId, amount, perPrice);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @Operation(summary = "管理员刷新指定商店中的物品")
    @SaCheckRole("ADMIN")
    @PostMapping("/refresh")
    public RestResponseVo<Void> refresh(@NotNull @RequestParam String store) {
        storeService.initBuilding(store);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @Operation(summary = "管理员刷新所有商店中的物品")
    @SaCheckRole("ADMIN")
    @PostMapping("/refreshAll")
    public RestResponseVo<Void> refreshAll() {
        storeService.refreshAll();
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

}

