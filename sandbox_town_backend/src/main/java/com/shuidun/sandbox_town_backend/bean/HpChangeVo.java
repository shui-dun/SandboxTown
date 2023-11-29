package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class HpChangeVo {
    @NonNull
    private String id;
    @NonNull
    private int originHp;
    @NonNull
    private int hpChange;
}
