package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.ChatFriendDo;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.ChatFriendMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatFriendService {
    private final ChatFriendMapper chatFriendMapper;

    public ChatFriendService(ChatFriendMapper chatFriendMapper) {
        this.chatFriendMapper = chatFriendMapper;
    }


    /** 拉黑用户 */
    @Transactional
    public void banUser(String username, String target) {
        // 判断好友关系是否存在
        ChatFriendDo chatFriend = chatFriendMapper.selectById(username, target);
        if (chatFriend == null) {
            throw new BusinessException(StatusCodeEnum.FRIEND_NOT_EXIST);
        }
        // 拉黑
        chatFriend.setBan(true);
        chatFriendMapper.update(chatFriend);
    }

    /** 解除拉黑 */
    @Transactional
    public void unbanUser(String username, String target) {
        // 判断好友关系是否存在
        ChatFriendDo chatFriend = chatFriendMapper.selectById(username, target);
        if (chatFriend == null) {
            throw new BusinessException(StatusCodeEnum.FRIEND_NOT_EXIST);
        }
        // 解除拉黑
        chatFriend.setBan(false);
        chatFriendMapper.update(chatFriend);
    }

}
