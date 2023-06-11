package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("sprite_item")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpriteItem {

    @TableId
    private String owner;

    @TableId
    private String itemId;

    private Integer itemCount;
}
