package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVo {
    @ApiModelProperty(value = "消息序列号，对应ChatMessageDo中的id字段")
    private Integer sn;

    private String source;

    @ApiModelProperty(value = "这个id是消息接收者的id，对应ChatFriendDo中的target字段")
    private String id;

    private ChatMsgTypeEnum type;

    private String message;

    private java.util.Date time;
}
