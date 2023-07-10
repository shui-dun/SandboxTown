package com.shuidun.sandbox_town_backend.bean ;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ItemLabelEnum;
import com.shuidun.sandbox_town_backend.enumeration.ItemTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("item_type_label")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemTypeLabelDo {

    private ItemTypeEnum itemType;

    private ItemLabelEnum label;
}
