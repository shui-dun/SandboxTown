package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("apple_picking")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApplePickingDo {
    private String sprite;

    private String tree;

    private Integer count;

    private java.util.Date pickTime;
}
