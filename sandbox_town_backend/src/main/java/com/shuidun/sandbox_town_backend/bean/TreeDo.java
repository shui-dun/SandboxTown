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
    @NonNull
    private String id;

    @NonNull
    private Integer applesCount;

    @NonNull
    private Integer maxApplesCount;

    @NonNull
    private Integer limitPerSprite;
}
