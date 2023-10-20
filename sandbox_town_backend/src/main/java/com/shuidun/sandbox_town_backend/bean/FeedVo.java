package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.FeedResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedVo {
    /** 驯服者id */
    private String id;

    /** 被驯服/喂食者id */
    private String targetId;

    /** 驯服/喂食结果 */
    FeedResultEnum result;
}
