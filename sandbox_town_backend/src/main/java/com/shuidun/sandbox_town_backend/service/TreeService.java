package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.ApplePicking;
import com.shuidun.sandbox_town_backend.bean.Tree;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.TreeMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class TreeService {

    private final TreeMapper treeMapper;

    private final ItemService itemService;

    private final String mapId;

    private final Random random = new Random();

    public TreeService(TreeMapper treeMapper, ItemService itemService, @Value("${mapId}") String mapId) {
        this.treeMapper = treeMapper;
        this.itemService = itemService;
        this.mapId = mapId;
    }

    /**
     * 角色摘苹果
     *
     * @return 是否摘苹果成功
     */
    @Transactional
    public void pickApple(String spriteId, String treeId) {
        // 查找树
        Tree tree = treeMapper.getTreeById(treeId);
        if (tree == null) {
            throw new BusinessException(StatusCodeEnum.ITEM_NOT_FOUND);
        }
        // 如果树已经被摘完了
        if (tree.getApplesCount() == 0) {
            throw new BusinessException(StatusCodeEnum.TREE_APPLE_PICKED);
        }
        // 查找角色摘苹果的信息
        ApplePicking applePicking = treeMapper.getApplePickingBySpriteIdAndTreeId(spriteId, treeId);
        // 如果信息为空，说明角色第一次摘苹果，插入一条记录
        if (applePicking == null) {
            applePicking = new ApplePicking();
            applePicking.setSprite(spriteId);
            applePicking.setTree(treeId);
            applePicking.setCount(1);
            applePicking.setPickTime(new java.util.Date());
            treeMapper.insertApplePicking(applePicking);
        } else {
            // 如果上次摘苹果时间超过10分钟
            if (applePicking.getPickTime().getTime() + 10 * 60 * 1000 < System.currentTimeMillis()) {
                applePicking.setCount(1);
                applePicking.setPickTime(new java.util.Date());
                treeMapper.updateApplePicking(applePicking);
            } else {
                // 如果上次摘苹果时间不超过一天
                // 如果摘苹果数量小于树的限制
                if (applePicking.getCount() < tree.getLimitPerSprite()) {
                    applePicking.setCount(applePicking.getCount() + 1);
                    applePicking.setPickTime(new java.util.Date());
                    treeMapper.updateApplePicking(applePicking);
                } else {
                    throw new BusinessException(StatusCodeEnum.PICK_APPLE_LIMIT_EXCEEDED);
                }
            }
        }
        // 树的苹果数目减一
        tree.setApplesCount(tree.getApplesCount() - 1);
        treeMapper.updateTree(tree);
        // 给角色增加苹果
        itemService.add(spriteId, "apple", 1);
    }

    /** 创建一棵树 */
    public void createRandomTree(String treeId) {
        Tree tree = new Tree();
        tree.setId(treeId);
        tree.setApplesCount(1 + random.nextInt(5));
        tree.setMaxApplesCount(tree.getApplesCount());
        tree.setLimitPerSprite(1);
        treeMapper.createTree(tree);
    }

    /** 刷新所有树的苹果数目等信息 */
    public void refreshTree() {
        // 查找所有树
        List<Tree> trees = treeMapper.getAllTrees();
        // 遍历所有树
        for (Tree tree : trees) {
            // 使得树的苹果数目等于最大苹果数目
            tree.setApplesCount(tree.getMaxApplesCount());
            // 更新树
            treeMapper.updateTree(tree);
        }
    }
}
