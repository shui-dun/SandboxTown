package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InteractDto {
    // 交互事件的发起者
    private String source;
    // 交互事件的目标
    private String target;
}
