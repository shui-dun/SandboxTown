package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("victory_attribute_reward")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VictoryAttributeRewardDo {

    @TableId
    private String spriteType;

    private Integer moneyInc;

    private Integer expInc;
}
