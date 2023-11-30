package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.FeedResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedVo {
    /** 驯服者id */
    private String id;

    /** 被驯服/喂食者id */
    private String targetId;

    /** 驯服/喂食结果 */
    private FeedResultEnum result;
}
