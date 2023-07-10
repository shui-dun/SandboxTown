package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.StoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // 得到某个商店的所有商品列表
    @GetMapping("/listByStore")
    public RestResponseVo<?> listByStore(String store) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, storeService.listByStore(store));
    }

    /**
     * 买入商品
     *
     * @param store  商店
     * @param item   商品id
     * @param amount 数量
     */
    @PostMapping("/buy")
    public RestResponseVo<?> buy(String store, ItemTypeEnum item, Integer amount) {
        storeService.buy(StpUtil.getLoginIdAsString(), store, item, amount);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    // 得到某个商店的某个商品的详细信息
    @GetMapping("/getByStoreAndItemType")
    public RestResponseVo<?> getByStoreAndItem(String store, ItemTypeEnum itemType) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, storeService.detailByStoreAndItemType(store, itemType));
    }

    // 得到用户向商店出售时的售价
    @GetMapping("/soldPrice")
    public RestResponseVo<?> soldPrice(String store, String itemId) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, storeService.soldPrice(store, itemId));
    }

    /** 出售物品 */
    @PostMapping("/sell")
    public RestResponseVo<?> sell(String store, String itemId, Integer amount, Integer perPrice) {
        storeService.sell(StpUtil.getLoginIdAsString(), store, itemId, amount, perPrice);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
    }

}

