package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("chat_message")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDo {

    @TableId
    private Integer id;

    private String source;

    private String target;

    private ChatMsgTypeEnum type;

    private String message;

    private java.util.Date time;
}
