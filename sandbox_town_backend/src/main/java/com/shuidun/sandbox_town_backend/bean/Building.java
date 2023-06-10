package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("building")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Building {

    @TableId
    private String id;

    private String type;

    private String map;

    private Integer level;

    private String owner;

    private Integer originX;

    private Integer originY;

    private Integer width;

    private Integer height;
}
