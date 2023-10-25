package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("chat_friend")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatFriendDo {

    @TableId
    private String user;

    @TableId
    private String friend;

    private Boolean ban;

    private String lastChatId;

    private String readChatId;

    private Integer unread;
}
