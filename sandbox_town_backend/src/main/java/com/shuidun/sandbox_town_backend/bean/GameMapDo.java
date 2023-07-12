package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("game_map")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMapDo {

    @TableId
    private String id;

    private String name;

    private Integer width;

    private Integer height;

    private Integer seed;

    /** 数据 */
    @TableField(exist = false)
    private int[][] data;
}
