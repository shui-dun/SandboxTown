package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.FeedResultEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedVo {
    @ApiModelProperty(value = "驯服者id")
    private String id;

    @ApiModelProperty(value = "被驯服/喂食者id")
    private String targetId;

    @ApiModelProperty(value = "驯服/喂食结果")
    private FeedResultEnum result;
}
