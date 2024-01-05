package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.ApplePickingDo;
import com.shuidun.sandbox_town_backend.bean.TreeDo;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.ApplePickingMapper;
import com.shuidun.sandbox_town_backend.mapper.TreeMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TreeService implements RefreshableBuilding {

    private final TreeMapper treeMapper;

    private final ApplePickingMapper applePickingMapper;

    private final ItemService itemService;

    @Value("${mapId}")
    private String mapId;

    public TreeService(TreeMapper treeMapper, ApplePickingMapper applePickingMapper, ItemService itemService) {
        this.treeMapper = treeMapper;
        this.applePickingMapper = applePickingMapper;
        this.itemService = itemService;
    }

    /**
     * 判断是否可以摘苹果，如果不可以摘苹果，则抛出异常
     */
    public void checkPickApple(String spriteId, String treeId) {
        // 查找树
        TreeDo tree = treeMapper.selectById(treeId);
        if (tree == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 如果树已经被摘完了
        if (tree.getApplesCount() == 0) {
            throw new BusinessException(StatusCodeEnum.TREE_APPLE_PICKED);
        }
        // 查找角色摘苹果的信息
        ApplePickingDo applePicking = applePickingMapper.selectById(spriteId, treeId);
        // 如果角色不是第一次摘苹果，并且上次摘苹果时间没有超过一天，并且如果摘苹果数量大于等于树的限制，说明不能摘苹果
        if (applePicking != null
                && applePicking.getPickTime().getTime() + Constants.DAY_TOTAL_DURATION >= System.currentTimeMillis()
                && applePicking.getCount() >= tree.getLimitPerSprite()) {
            throw new BusinessException(StatusCodeEnum.PICK_APPLE_LIMIT_EXCEEDED);
        }
    }


    /**
     * 角色摘苹果
     */
    @Transactional
    public void pickApple(String spriteId, String treeId) {
        // 查找树
        TreeDo tree = treeMapper.selectById(treeId);
        if (tree == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 如果树已经被摘完了
        if (tree.getApplesCount() == 0) {
            throw new BusinessException(StatusCodeEnum.TREE_APPLE_PICKED);
        }
        // 查找角色摘苹果的信息
        ApplePickingDo applePicking = applePickingMapper.selectById(spriteId, treeId);
        // 如果信息为空，说明角色第一次摘苹果，插入一条记录
        if (applePicking == null) {
            applePicking = new ApplePickingDo(
                    spriteId,
                    treeId,
                    1,
                    new java.util.Date()
            );
            applePickingMapper.insert(applePicking);
        } else {
            // 如果上次摘苹果时间超过一天
            if (applePicking.getPickTime().getTime() + Constants.DAY_TOTAL_DURATION < System.currentTimeMillis()) {
                applePicking.setCount(1);
                applePicking.setPickTime(new java.util.Date());
                applePickingMapper.update(applePicking);
            } else {
                // 如果上次摘苹果时间不超过一天
                // 如果摘苹果数量小于树的限制
                if (applePicking.getCount() < tree.getLimitPerSprite()) {
                    applePicking.setCount(applePicking.getCount() + 1);
                    applePicking.setPickTime(new java.util.Date());
                    applePickingMapper.update(applePicking);
                } else {
                    throw new BusinessException(StatusCodeEnum.PICK_APPLE_LIMIT_EXCEEDED);
                }
            }
        }
        // 树的苹果数目减一
        tree.setApplesCount(tree.getApplesCount() - 1);
        treeMapper.updateById(tree);
        // 给角色增加苹果
        itemService.add(spriteId, ItemTypeEnum.APPLE, 1);
    }

    /** 创建一棵树 */
    public void createRandomTree(String treeId) {
        int appleCount = 1 + GameCache.random.nextInt(5);
        TreeDo tree = new TreeDo(
                treeId,
                appleCount,
                appleCount,
                1
        );
        treeMapper.insert(tree);
    }

    /** 刷新所有树的苹果数目等信息 */
    @Override
    public void refreshAll() {
        // 查找所有树
        List<TreeDo> trees = treeMapper.selectList(null);
        // 遍历所有树
        for (TreeDo tree : trees) {
            // 使得树的苹果数目等于最大苹果数目
            tree.setApplesCount(tree.getMaxApplesCount());
            // 更新树
            treeMapper.updateById(tree);
        }
    }
}
