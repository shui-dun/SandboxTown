package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("tree")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeDo {

    @TableId
    private String id;

    private Integer applesCount;

    private Integer maxApplesCount;

    private Integer limitPerSprite;
}
