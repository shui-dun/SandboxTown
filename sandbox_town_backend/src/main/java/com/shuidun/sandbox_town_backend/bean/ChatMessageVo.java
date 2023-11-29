package com.shuidun.sandbox_town_backend.bean;

import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVo {
    /** 消息序列号，对应ChatMessageDo中的id字段 */
    @NonNull
    private Integer sn;

    @NonNull
    private String source;

    /** 这个id是消息接收者的id，对应ChatFriendDo中的target字段 */
    @NonNull
    private String id;

    @NonNull
    private ChatMsgTypeEnum type;

    @NonNull
    private String message;

    @NonNull
    private java.util.Date time;
}
