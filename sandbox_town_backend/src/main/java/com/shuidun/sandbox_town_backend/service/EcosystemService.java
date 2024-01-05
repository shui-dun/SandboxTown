package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.BuildingDo;
import com.shuidun.sandbox_town_backend.bean.BuildingTypeDo;
import com.shuidun.sandbox_town_backend.enumeration.BuildingTypeEnum;
import com.shuidun.sandbox_town_backend.mapper.BuildingMapper;
import com.shuidun.sandbox_town_backend.mapper.BuildingTypeMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.mixin.GameCache;
import com.shuidun.sandbox_town_backend.utils.UUIDNameGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
/**
 * 管理生态环境的创建、刷新等
 */
public class EcosystemService {

    private final BuildingMapper buildingMapper;

    private final BuildingTypeMapper buildingTypeMapper;

    private final SpriteService spriteService;

    private final TreeService treeService;

    private final StoreService storeService;

    @Value("${mapId}")
    private String mapId;

    private final RedisTemplate<String, Object> redisTemplate;

    private final GameMapService gameMapService;

    private final List<RefreshableBuilding> refreshableBuildings;

    public EcosystemService(BuildingMapper buildingMapper, BuildingTypeMapper buildingTypeMapper, SpriteService spriteService, TreeService treeService, StoreService storeService, RedisTemplate<String, Object> redisTemplate, GameMapService gameMapService, List<RefreshableBuilding> refreshableBuildings) {
        this.buildingMapper = buildingMapper;
        this.buildingTypeMapper = buildingTypeMapper;
        this.spriteService = spriteService;
        this.treeService = treeService;
        this.storeService = storeService;
        this.redisTemplate = redisTemplate;
        this.gameMapService = gameMapService;
        this.refreshableBuildings = refreshableBuildings;
    }


    /** 刷新所有可刷新的建筑 */
    public void refreshAllBuildings() {
        for (RefreshableBuilding refreshableBuilding : refreshableBuildings) {
            refreshableBuilding.refreshAll();
            log.info("refreshAllBuildings: {}", refreshableBuilding.getClass().getName());
        }
    }

    /**
     * 将所有建筑物放置在地图上
     *
     * @return 是否至少存在一个建筑物
     */
    public boolean placeAllBuildingsOnMap() {
        // 建筑物的黑白图的字典
        var buildingTypes = buildingTypeMapper.selectList(null);
        for (BuildingTypeDo buildingType : buildingTypes) {
            BuildingTypeEnum buildingTypeId = buildingType.getId();
            String imagePath = buildingType.getImagePath();
            try {
                BufferedImage image = ImageIO.read(new ClassPathResource(imagePath).getInputStream());
                GameCache.buildingTypesImages.put(buildingTypeId, image);
            } catch (IOException e) {
                log.info("读取建筑物黑白图失败", e);
            }
        }

        // 获取当前地图上的所有建筑物
        var buildings = buildingMapper.selectByMapId(mapId);

        // 将建筑放置在地图上
        for (BuildingDo building : buildings) {
            gameMapService.placeBuildingOnMap(building);
        }
        return !buildings.isEmpty();
    }

    /** 随机创建指定数目的建筑以及其附属的生态环境 */
    public void createEnvironment(int nBuildings) {
        // 得到所有建筑类型
        var buildingTypes = buildingTypeMapper.selectList(null);
        // 首先，所有类型的建筑都有一个
        List<BuildingTypeDo> buildingTypesToBePlaced = new ArrayList<>(buildingTypes);
        // 计算总稀有度
        double totalRarity = 0;
        for (BuildingTypeDo buildingType : buildingTypes) {
            totalRarity += buildingType.getRarity();
        }
        // 随后，根据建筑类型的稀有度，根据轮盘赌算法，随机生成nBuildings-buildingTypes.size()个建筑
        for (int i = 0; i < nBuildings - buildingTypes.size(); ++i) {
            // 计算轮盘赌的随机值
            double randomValue = Math.random() * totalRarity;
            // 计算轮盘赌的结果
            double sum = 0;
            int index = 0;
            for (BuildingTypeDo buildingType : buildingTypes) {
                sum += buildingType.getRarity();
                if (sum >= randomValue) {
                    index = buildingTypes.indexOf(buildingType);
                    break;
                }
            }
            // 将轮盘赌的结果加入建筑列表
            buildingTypesToBePlaced.add(buildingTypes.get(index));
        }
        // 生成建筑
        for (int i = 0; i < buildingTypesToBePlaced.size(); ++i) {
            // 建筑类型
            BuildingTypeDo buildingType = buildingTypesToBePlaced.get(i);
            // 随机生成建筑的左上角
            double x = Math.random() * (GameCache.map.length - 8) * Constants.PIXELS_PER_GRID;
            double y = Math.random() * (GameCache.map[0].length - 8) * Constants.PIXELS_PER_GRID;
            // 随机生成建筑的宽高，在基础宽高的基础上波动（0.8倍到1.2倍）
            double scale = Math.random() * 0.4 + 0.8;
            // 创建建筑对象
            BuildingDo building = new BuildingDo();
            building.setId(UUIDNameGenerator.generateItemName(buildingType.getId().name()));
            building.setType(buildingType.getId());
            building.setMap(mapId);
            building.setLevel(1);
            building.setOriginX(x);
            building.setOriginY(y);
            building.setWidth(buildingType.getBasicWidth() * scale);
            building.setHeight(buildingType.getBasicHeight() * scale);
            // 判断是否与其他建筑重叠
            if (!gameMapService.isBuildingOverlapStrict(building)) {
                // 如果不重叠，添加建筑到数据库
                buildingMapper.insert(building);
                // 放置建筑
                gameMapService.placeBuildingOnMap(building);
                // 如果是树
                if (buildingType.getId().equals(BuildingTypeEnum.TREE)) {
                    treeService.createRandomTree(building.getId());
                } else if (buildingType.getId().equals(BuildingTypeEnum.STORE)) { // 如果是商店
                    // 刷新商店商品
                    storeService.refresh(building.getId());
                }
            } else {
                log.info("建筑重叠，重新生成建筑");
                // 如果重叠，重新生成建筑
                --i;
            }
        }
        // 生成精灵
        spriteService.refreshAllSprites();

        // 删除建筑的缓存
        // 之所以不直接使用@CacheEvict(value = "building::buildings", key = "#mapId")
        // 是为了修复GameInitializer的构造方法中调用createEnvironment时，@CacheEvict注解不生效的问题（看起来一个component必须在构造之后才能使用注解）
        redisTemplate.delete("building::buildings::" + mapId);
    }
}
