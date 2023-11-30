package com.shuidun.sandbox_town_backend.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@TableName("chat_friend")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatFriendDo {

    private String user;

    private String friend;

    private Boolean ban;

    private Integer lastChatId;

    @Nullable
    private Integer readChatId;

    private Integer unread;
}
