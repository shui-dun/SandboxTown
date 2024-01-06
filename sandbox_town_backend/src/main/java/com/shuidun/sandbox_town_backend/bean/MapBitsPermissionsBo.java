package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.MapBitEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MapBitsPermissionsBo {
    /** 障碍物，即不能通过的地图点元素，优先级最高 */
    private int obstacles;

    /** 允许的地图点，标记只能在特定元素周围移动的地图点，优先级第2 */
    private int allow;

    /** 禁止的地图点，与obstacles类似，但是优先级最低 */
    private int forbid;

    /**
     * 将一组MapBit枚举值转换为一个整数，表示地图点的元素组合。
     * 该方法利用位运算来合并多个MapBitEnum值。
     *
     * @param bits 一个或多个MapBitEnum枚举值。
     * @return 一个整数，其位表示对应于传入的MapBitEnum值
     */
    public static int mapBitArrayToInt(MapBitEnum... bits) {
        int result = 0;
        for (MapBitEnum bit : bits) {
            result |= (1 << bit.ordinal());
        }
        return result;
    }

    /** 默认的obstacles */
    public static final int DEFAULT_OBSTACLES = mapBitArrayToInt(MapBitEnum.WALL, MapBitEnum.BUILDING);

    /** 默认的allow */
    public static final int DEFAULT_ALLOW = 0;

    /** 默认的forbid */
    public static final int DEFAULT_FORBID = 0;

    /** 默认的地图移动权限 */
    public static final MapBitsPermissionsBo DEFAULT_MAP_BITS_PERMISSIONS = new MapBitsPermissionsBo(DEFAULT_OBSTACLES, DEFAULT_ALLOW, DEFAULT_FORBID);
}
