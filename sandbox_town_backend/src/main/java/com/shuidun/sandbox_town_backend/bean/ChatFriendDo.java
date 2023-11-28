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

    private String user;

    private String friend;

    private Boolean ban;

    private Integer lastChatId;

    private Integer readChatId;

    private Integer unread;
}
