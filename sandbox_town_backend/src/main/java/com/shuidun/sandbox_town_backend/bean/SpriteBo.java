package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.SpriteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.lang.Nullable;

import java.util.List;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpriteBo extends SpriteDo {
    SpriteTypeDo spriteTypeDo;

    /** 缓存是否dirty */
    private boolean dirty;

    @Schema(description = """
            xxInc是查询精灵的装备和效果等信息后得到的字段
            玩家最后的属性值等于原先的属性值加上增量（装备或手持装备导致的属性变化）
            但注意: 没有moneyInc、expInc、levelInc
            """)
    private Integer hungerInc;

    private Integer hpInc;

    private Integer attackInc;

    private Integer defenseInc;

    private Integer speedInc;

    private Integer visionRangeInc;

    private Integer attackRangeInc;

    @Schema(description = "效果列表")
    private List<SpriteEffectBo> effects;

    @Schema(description = "装备列表")
    private List<ItemBo> equipments;

    private Double vx;

    private Double vy;

    @Schema(description = "上次更新坐标时间")
    private Long lastMoveTime;

    @Schema(description = "上次与其他精灵交互时间")
    @Nullable
    private Long lastInteractTime;

    @Schema(description = "上次与其他精灵交互的序列号")
    @Nullable
    private Integer lastInteractSn;

    private SpriteStatus status;

    @Schema(description = "目标精灵id")
    @Nullable
    private String targetSpriteId;

    @Schema(description = "目标建筑id")
    @Nullable
    private String targetBuildingId;

    @Schema(description = "目标（既非精灵也非建筑）的x坐标")
    @Nullable
    private Double targetX;

    @Schema(description = "目标（既非精灵也非建筑）的y坐标")
    @Nullable
    private Double targetY;

    @Schema(description = "交互目标精灵id")
    @Nullable
    private String interactSpriteId;

    public static SpriteBo fromSpriteDo(SpriteDo spriteDo) {
        SpriteBo b = new SpriteBo();
        // 一开始是dirty的
        b.setDirty(true);
        b.setId(spriteDo.getId());
        b.setType(spriteDo.getType());
        b.setOwner(spriteDo.getOwner());
        b.setMoney(spriteDo.getMoney());
        b.setExp(spriteDo.getExp());
        b.setLevel(spriteDo.getLevel());
        b.setHunger(spriteDo.getHunger());
        b.setHp(spriteDo.getHp());
        b.setAttack(spriteDo.getAttack());
        b.setDefense(spriteDo.getDefense());
        b.setSpeed(spriteDo.getSpeed());
        b.setVisionRange(spriteDo.getVisionRange());
        b.setAttackRange(spriteDo.getAttackRange());
        b.setX(spriteDo.getX());
        b.setY(spriteDo.getY());
        b.setWidth(spriteDo.getWidth());
        b.setHeight(spriteDo.getHeight());
        b.setMap(spriteDo.getMap());
        return b;
    }
}
