package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.FeedResultEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedVo {
    @Schema(description = "驯服者id")
    private String id;

    @Schema(description = "被驯服/喂食者id")
    private String targetId;

    @Schema(description = "驯服/喂食结果")
    private FeedResultEnum result;
}
