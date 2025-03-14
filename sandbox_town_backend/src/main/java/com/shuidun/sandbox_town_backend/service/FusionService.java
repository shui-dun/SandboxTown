package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.*;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.FusionMapper;
import com.shuidun.sandbox_town_backend.mapper.FusionMaterialMapper;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FusionService {
    private final FusionMapper fusionMapper;
    private final FusionMaterialMapper fusionMaterialMapper;
    private final ItemService itemService;

    public FusionService(FusionMapper fusionMapper, FusionMaterialMapper fusionMaterialMapper, ItemService itemService) {
        this.fusionMapper = fusionMapper;
        this.fusionMaterialMapper = fusionMaterialMapper;
        this.itemService = itemService;
    }

    /**
     * Check what would be the result of fusion with given items
     */
    public FusionResultVo checkFusion(String spriteId, FusionRequestDto request) {
        // Get all items and their types
        Map<String, ItemBo> items = request.getItems().keySet().stream()
            .map(itemService::getItemDetailById)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(ItemBo::getId, item -> item));
        
        // Verify item ownership and check we have enough of each
        for (Map.Entry<String, Integer> entry : request.getItems().entrySet()) {
            ItemBo item = items.get(entry.getKey());
            if (item == null) {
                throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
            }
            if (!item.getOwner().equals(spriteId)) {
                throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
            }
            if (item.getItemCount() < entry.getValue()) {
                throw new BusinessException(StatusCodeEnum.ITEM_NOT_ENOUGH);
            }
        }

        // Group items by type and sum quantities
        Map<ItemTypeEnum, Integer> itemTypeCounts = new HashMap<>();
        for (Map.Entry<String, Integer> entry : request.getItems().entrySet()) {
            ItemBo item = items.get(entry.getKey());
            itemTypeCounts.merge(item.getItemType(), entry.getValue(), Integer::sum);
        }

        // Find a matching fusion recipe
        for (FusionDo fusion : fusionMapper.selectList(null)) {
            List<FusionMaterialDo> materials = fusionMaterialMapper.selectByFusionId(fusion.getId());
            
            // Check if all materials match in type and quantity
            boolean matches = true;
            Map<String, Integer> deductedItems = new HashMap<>();
            
            for (FusionMaterialDo material : materials) {
                int availableCount = itemTypeCounts.getOrDefault(material.getItemName(), 0);
                if (availableCount < material.getQuantity()) {
                    matches = false;
                    break;
                }
                
                // Find items of this type and record how many will be deducted
                int remainingToDeduct = material.getQuantity();
                for (Map.Entry<String, ItemBo> itemEntry : items.entrySet()) {
                    if (itemEntry.getValue().getItemType() == material.getItemName()) {
                        int availableInThisItem = request.getItems().get(itemEntry.getKey());
                        int deduct = Math.min(availableInThisItem, remainingToDeduct);
                        if (deduct > 0) {
                            deductedItems.put(itemEntry.getKey(), deduct); // 记录要扣除的数量
                            remainingToDeduct -= deduct;
                            if (remainingToDeduct == 0) {
                                break;
                            }
                        }
                    }
                }
            }

            if (matches) {
                return new FusionResultVo(deductedItems, fusion.getResultItemId());
            }
        }

        throw new BusinessException(StatusCodeEnum.NO_FUSION_RECIPE);
    }

    /**
     * Execute fusion with given items
     */
    @Transactional
    public void executeFusion(String spriteId, FusionRequestDto request) {
        FusionResultVo result = checkFusion(spriteId, request);
        
        // Remove consumed items based on deductedItems
        for (Map.Entry<String, Integer> entry : result.getDeductedItems().entrySet()) {
            ItemDo item = itemService.getItemById(entry.getKey());
            if (item == null || !item.getOwner().equals(spriteId)) {
                throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
            }
            
            int newCount = item.getItemCount() - entry.getValue();
            if (newCount < 0) {
                throw new BusinessException(StatusCodeEnum.ITEM_NOT_ENOUGH);
            }
            
            item.setItemCount(newCount);
            itemService.updateItem(item);
        }

        // Add result item
        itemService.add(spriteId, result.getResultItem(), 1);
        
        // Notify via WebSocket about item changes
        WSMessageSender.addResponse(WSResponseEnum.ITEM_GAIN, 
            new ItemGainVo(spriteId, result.getResultItem(), 1));
    }
}