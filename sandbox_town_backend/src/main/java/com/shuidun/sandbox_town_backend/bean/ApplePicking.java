package com.shuidun.sandbox_town_backend.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplePicking {

    private String sprite;

    private String tree;

    private Integer count;

    private java.util.Date pickTime;
}
