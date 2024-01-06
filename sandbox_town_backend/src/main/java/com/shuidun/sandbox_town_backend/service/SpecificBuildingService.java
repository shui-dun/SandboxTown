package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.BuildingDo;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;

/**
 * 可刷新的建筑类型服务，例如TreeService、StoreService等
 */
public interface SpecificBuildingService {
    /**
     * 刷新所有建筑
     */
    void refreshAll();

    /** 初始化建筑 */
    void initBuilding(BuildingDo building);

    /** 得到对应的建筑类型 */
    BuildingTypeEnum getType();
}
