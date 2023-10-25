package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.ChatFriendDo;
import com.shuidun.sandbox_town_backend.bean.ChatMessageDo;
import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.ChatFriendMapper;
import com.shuidun.sandbox_town_backend.mapper.ChatMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ChatMessageService {
    private final ChatMessageMapper chatMessageMapper;

    private final ChatFriendMapper chatFriendMapper;

    public ChatMessageService(ChatMessageMapper chatMessageMapper, ChatFriendMapper chatFriendMapper) {
        this.chatMessageMapper = chatMessageMapper;
        this.chatFriendMapper = chatFriendMapper;
    }

    /**
     * 判断某用户对某消息是否用权限
     *
     * @return 返回消息
     */
    private ChatMessageDo checkPermission(String username, String messageId) {
        // 判断消息是否存在
        ChatMessageDo chatMessage = chatMessageMapper.selectById(messageId);
        if (chatMessage == null) {
            throw new BusinessException(StatusCodeEnum.MESSAGE_NOT_EXIST);
        }
        // 只有消息的发送者和接收者才能读写消息
        String source = chatMessage.getSource();
        String target = chatMessage.getTarget();
        if (!source.equals(username) && !target.equals(username)) {
            throw new BusinessException(StatusCodeEnum.NO_PERMISSION);
        }
        return chatMessage;
    }

    /** 删除消息 */
    @Transactional
    public void deleteMessage(String username, String messageId) {
        checkPermission(username, messageId);
        // 删除消息
        chatMessageMapper.deleteById(messageId);
    }

    /** 编辑已有消息（目前只支持编辑文本消息） */
    @Transactional
    public void editMessage(String username, String messageId, String newContent) {
        ChatMessageDo message = checkPermission(username, messageId);
        // 判断是否是文本消息
        if (!message.getType().equals(ChatMsgTypeEnum.TEXT)) {
            throw new BusinessException(StatusCodeEnum.MESSAGE_TYPE_NOT_SUPPORT);
        }
        // 编辑消息
        message.setMessage(newContent);
        chatMessageMapper.updateById(message);
    }

    /** 已读消息 */
    @Transactional
    public void readMessage(String username, String messageId) {
        ChatMessageDo message = checkPermission(username, messageId);
        ChatFriendDo chatFriend = chatFriendMapper.selectById(username, message.getSource().equals(username) ? message.getTarget() : message.getSource());
        // 判断该消息是否比上次已读消息新
        if (Long.parseLong(messageId) <= Long.parseLong(chatFriend.getReadChatId())) {
            return;
        }
        // 更新最后一条已读消息
        chatFriend.setReadChatId(messageId);
        // 更新未读消息数量
        chatFriend.setUnread(chatMessageMapper.countNewerThanId(chatFriend.getUser(), chatFriend.getFriend(), chatFriend.getReadChatId()));
        // 写入更新后的好友关系
        chatFriendMapper.update(chatFriend);
    }

    /** 删除指定时间前的消息 */
    @Transactional
    public void deleteMessageBefore(Date time) {
        // 删除消息
        chatMessageMapper.deleteMessageBefore(time);
    }

    /**
     * 加载两用户在某个消息前的指定长度的消息列表
     *
     * @param username  用户名
     * @param friend    好友名
     * @param messageId 消息id
     * @param count     加载数量
     * @return 消息列表
     */
    public List<ChatMessageDo> loadMessageBefore(String username, String friend, String messageId, Integer count) {
        return chatMessageMapper.loadMessageBeforeId(username, friend, messageId, count);
    }

    /**
     * 加载两用户在某个消息前的、包含某个关键字的、指定长度的消息列表（目前只支持文本消息）
     *
     * @param username  用户名
     * @param friend    好友名
     * @param messageId 消息id
     * @param count     加载数量
     * @param keyword   关键字
     * @return 消息列表
     */
    public List<ChatMessageDo> loadMessageBeforeWithKeyword(String username, String friend, String messageId, Integer count, String keyword) {
        return chatMessageMapper.loadMessageBeforeIdWithKeyword(username, friend, messageId, count, keyword, ChatMsgTypeEnum.TEXT);
    }

}
