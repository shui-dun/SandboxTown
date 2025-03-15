package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.FusionMapper;
import com.shuidun.sandbox_town_backend.mapper.FusionMaterialMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FusionServiceTest {

    @Mock
    private FusionMapper fusionMapper;

    @Mock
    private FusionMaterialMapper fusionMaterialMapper;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private FusionService fusionService;

    private final String SPRITE_ID = "testSprite";
    private final String ITEM_ID_1 = "item1";
    private final String ITEM_ID_2 = "item2";
    private final String ITEM_ID_3 = "item3";
    private final ItemTypeEnum MATERIAL_TYPE_1 = ItemTypeEnum.IRON;
    private final ItemTypeEnum MATERIAL_TYPE_2 = ItemTypeEnum.WOOD;
    private final ItemTypeEnum RESULT_TYPE = ItemTypeEnum.IRON_HELMET;

    /**
     * 准备合成请求
     */
    private FusionRequestDto prepareFusionRequest(Map<String, Integer> items) {
        FusionRequestDto request = new FusionRequestDto();
        request.setItems(items);
        return request;
    }

    /**
     * 模拟物品详情
     */
    private void mockItems(Map<String, ItemBo> itemMap) {
        itemMap.forEach((itemId, item) -> {
            when(itemService.getItemDetailById(itemId)).thenReturn(item);
        });
    }

    /**
     * 模拟合成配方和材料
     */
    private void mockFusionRecipe(ItemTypeEnum resultType, Map<ItemTypeEnum, Integer> materials) {
        // 模拟合成配方
        FusionDo fusion = new FusionDo(1, RESULT_TYPE);
        when(fusionMapper.selectList(any())).thenReturn(Arrays.asList(fusion));

        // 模拟合成材料需求
        AtomicInteger counter = new AtomicInteger(1);
        List<FusionMaterialDo> materialList = materials.entrySet().stream()
            .map(entry -> new FusionMaterialDo(
                counter.getAndIncrement(),
                1,
                entry.getKey(),
                entry.getValue()
            ))
            .collect(Collectors.toList());
        when(fusionMaterialMapper.selectByFusionId(1)).thenReturn(materialList);
    }

    /**
     * 测试正常的合成流程
     */
    @Test
    void testCheckFusion_Success() {
        // 准备合成请求
        Map<String, Integer> items = Map.of(
            ITEM_ID_1, 2,
            ITEM_ID_2, 2
        );
        FusionRequestDto request = prepareFusionRequest(items);

        // 准备物品详情
        Map<String, ItemBo> itemMap = Map.of(
            ITEM_ID_1, createItemBo(ITEM_ID_1, SPRITE_ID, MATERIAL_TYPE_1, 3),
            ITEM_ID_2, createItemBo(ITEM_ID_2, SPRITE_ID, MATERIAL_TYPE_2, 2)
        );
        mockItems(itemMap);

        // 准备合成配方
        Map<ItemTypeEnum, Integer> materials = Map.of(
            MATERIAL_TYPE_1, 2,
            MATERIAL_TYPE_2, 1
        );
        mockFusionRecipe(RESULT_TYPE, materials);

        // 执行测试
        FusionResultVo result = fusionService.checkFusion(SPRITE_ID, request);

        // 验证结果
        assertNotNull(result);
        assertEquals(RESULT_TYPE, result.getResultItem());
        assertEquals(2, result.getDeductedItems().get(ITEM_ID_1));
        assertEquals(1, result.getDeductedItems().get(ITEM_ID_2));
    }

    /**
     * 测试物品数量不足的情况
     */
    @Test
    void testCheckFusion_NotEnoughItems() {
        // 准备测试数据
        FusionRequestDto request = new FusionRequestDto();
        Map<String, Integer> items = Map.of(ITEM_ID_1, 3);
        request.setItems(items);

        // 模拟物品详情 - 只有2个铁
        ItemBo item1 = createItemBo(ITEM_ID_1, SPRITE_ID, MATERIAL_TYPE_1, 2);
        when(itemService.getItemDetailById(ITEM_ID_1)).thenReturn(item1);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class,
            () -> fusionService.checkFusion(SPRITE_ID, request));
        assertEquals(StatusCodeEnum.ITEM_NOT_ENOUGH, exception.getStatusCode());
    }

    /**
     * 测试物品不属于该精灵的情况
     */
    @Test
    void testCheckFusion_WrongOwner() {
        // 准备测试数据
        FusionRequestDto request = new FusionRequestDto();
        Map<String, Integer> items = Map.of(ITEM_ID_1, 1);
        request.setItems(items);

        // 模拟物品详情 - 物品属于其他精灵
        ItemBo item1 = createItemBo(ITEM_ID_1, "otherSprite", MATERIAL_TYPE_1, 2);
        when(itemService.getItemDetailById(ITEM_ID_1)).thenReturn(item1);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class,
            () -> fusionService.checkFusion(SPRITE_ID, request));
        assertEquals(StatusCodeEnum.NO_PERMISSION, exception.getStatusCode());
    }

    /**
     * 测试使用多个同类型物品进行合成的情况
     */
    @Test
    void testCheckFusion_MultipleItemsSameType() {
        // 准备合成请求
        Map<String, Integer> items = Map.of(
            ITEM_ID_1, 1,
            ITEM_ID_3, 1,
            ITEM_ID_2, 1
        );
        FusionRequestDto request = prepareFusionRequest(items);

        // 准备物品详情
        Map<String, ItemBo> itemMap = Map.of(
            ITEM_ID_1, createItemBo(ITEM_ID_1, SPRITE_ID, MATERIAL_TYPE_1, 1),
            ITEM_ID_2, createItemBo(ITEM_ID_2, SPRITE_ID, MATERIAL_TYPE_2, 1),
            ITEM_ID_3, createItemBo(ITEM_ID_3, SPRITE_ID, MATERIAL_TYPE_1, 1)
        );
        mockItems(itemMap);

        // 准备合成配方
        Map<ItemTypeEnum, Integer> materials = Map.of(
            MATERIAL_TYPE_1, 2,
            MATERIAL_TYPE_2, 1
        );
        mockFusionRecipe(RESULT_TYPE, materials);

        // 执行测试
        FusionResultVo result = fusionService.checkFusion(SPRITE_ID, request);

        // 验证结果
        assertNotNull(result);
        assertEquals(RESULT_TYPE, result.getResultItem());
        assertEquals(1, result.getDeductedItems().get(ITEM_ID_1));
        assertEquals(1, result.getDeductedItems().get(ITEM_ID_3));
        assertEquals(1, result.getDeductedItems().get(ITEM_ID_2));
    }

    /**
     * 创建测试用ItemBo对象
     */
    private ItemBo createItemBo(String id, String owner, ItemTypeEnum type, int count) {
        ItemBo item = new ItemBo();
        item.setId(id);
        item.setOwner(owner);
        item.setItemType(type);
        item.setItemCount(count);
        return item;
    }

    /**
     * 创建测试用ItemDo对象
     */
    private ItemDo createItemDo(String id, String owner, ItemTypeEnum type, int count) {
        ItemDo item = new ItemDo();
        item.setId(id);
        item.setOwner(owner);
        item.setItemType(type);
        item.setItemCount(count);
        return item;
    }
}