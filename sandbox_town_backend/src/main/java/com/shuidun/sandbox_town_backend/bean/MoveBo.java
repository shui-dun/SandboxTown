package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MoveBo {

    /** 是否进行移动 */
    private boolean move;

    /** 目标点x坐标 */
    private double x;

    /** 目标点y坐标 */
    private double y;

    /** 目标建筑ID（如果目标点是建筑） */
    @Nullable
    private String destBuildingId;

    /** 目标精灵（如果目标点是精灵） */
    @Nullable
    private SpriteWithTypeBo destSprite;

    /** 是否与目标保持一定距离 */
    private boolean keepDistance;

    /** 保持一定距离 */
    public MoveBo keepDistance() {
        this.keepDistance = true;
        this.destBuildingId = null;
        this.destSprite = null;
        return this;
    }

    private static final MoveBo EMPTY = new MoveBo(false, 0.0, 0.0, null, null, false);

    /** 不进行移动时，返回该对象 */
    public static MoveBo empty() {
        return EMPTY;
    }

    /** 移动到指定点 */
    public static MoveBo moveToPoint(double x, double y) {
        return new MoveBo(true, x, y, null, null, false);
    }

    /** 移动到指定建筑 */
    public static MoveBo moveToBuilding(String buildingId, double x, double y) {
        return new MoveBo(true, x, y, buildingId, null, false);
    }

    /** 移动到指定建筑 */
    public static MoveBo moveToBuilding(BuildingDo building) {
        return new MoveBo(true, building.getOriginX() + building.getWidth() / 2, building.getOriginY() + building.getHeight() / 2, building.getId(), null, false);
    }

    /** 移动到指定精灵 */
    public static MoveBo moveToSprite(SpriteWithTypeBo sprite) {
        return new MoveBo(true, sprite.getX(), sprite.getY(), null, sprite, false);
    }
}
