package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("item_type_label")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemTypeLabel {

    private String itemType;

    private String label;
}
