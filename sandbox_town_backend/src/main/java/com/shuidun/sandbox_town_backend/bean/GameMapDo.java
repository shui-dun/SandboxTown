package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@TableName("game_map")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameMapDo {

    @TableId
    @NonNull
    private String id;

    @NonNull
    private String name;

    @NonNull
    private Integer width;

    @NonNull
    private Integer height;

    @NonNull
    private Integer seed;
}
