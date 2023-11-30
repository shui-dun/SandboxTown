package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.EffectEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("effect")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EffectDo {

    @TableId
    private EffectEnum id;

    private String name;

    private String description;
}
