package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@TableName("chat_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDo {

    /** 设置type = IdType.AUTO才能自增 */
    @TableId(type = IdType.AUTO)
    @NonNull
    private Integer id;

    @NonNull
    private String source;

    @NonNull
    private String target;

    @NonNull
    private ChatMsgTypeEnum type;

    @NonNull
    private String message;

    @NonNull
    private java.util.Date time;
}
