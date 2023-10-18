package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.TameResultEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TameVo {
    /** 驯服者id */
    private String id;

    /** 被驯服者id */
    private String targetId;

    /** 驯服结果 */
    TameResultEnum result;
}
