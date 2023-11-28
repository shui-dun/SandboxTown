package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.ChatFriendDo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;

@Mapper
public interface ChatFriendMapper extends BaseMapper<ChatFriendDo> {
    /** 根据用户名和好友名查询好友关系 */
    @Nullable
    default ChatFriendDo selectById(String user, String friend) {
        return selectOne(new LambdaQueryWrapper<ChatFriendDo>()
                .eq(ChatFriendDo::getUser, user)
                .eq(ChatFriendDo::getFriend, friend));
    }


    /** 更新好友关系 */
    default void update(ChatFriendDo chatFriend) {
        update(chatFriend, new LambdaQueryWrapper<ChatFriendDo>()
                .eq(ChatFriendDo::getUser, chatFriend.getUser())
                .eq(ChatFriendDo::getFriend, chatFriend.getFriend()));
    }
}
