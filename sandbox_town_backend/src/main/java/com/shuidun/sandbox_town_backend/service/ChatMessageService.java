package com.shuidun.sandbox_town_backend.service;

import com.shuidun.sandbox_town_backend.bean.ChatFriendDo;
import com.shuidun.sandbox_town_backend.bean.ChatMessageDo;
import com.shuidun.sandbox_town_backend.bean.ChatMessageVo;
import com.shuidun.sandbox_town_backend.bean.WSResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.enumeration.WSResponseEnum;
import com.shuidun.sandbox_town_backend.exception.BusinessException;
import com.shuidun.sandbox_town_backend.mapper.ChatFriendMapper;
import com.shuidun.sandbox_town_backend.mapper.ChatMessageMapper;
import com.shuidun.sandbox_town_backend.mapper.UserMapper;
import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.utils.SecureNameGenerator;
import com.shuidun.sandbox_town_backend.websocket.WSMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ChatMessageService {
    private final ChatMessageMapper chatMessageMapper;

    private final ChatFriendMapper chatFriendMapper;

    private final UserMapper userMapper;

    private final String userUploadPath;

    public ChatMessageService(@Value("${userUploadPath}") String userUploadPath, ChatMessageMapper chatMessageMapper, ChatFriendMapper chatFriendMapper, UserMapper userMapper) {
        this.userUploadPath = userUploadPath;
        // 如果用户上传文件的目录不存在，则递归地创建目录
        File file = new File(userUploadPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        this.chatMessageMapper = chatMessageMapper;
        this.chatFriendMapper = chatFriendMapper;
        this.userMapper = userMapper;
    }

    /**
     * 判断某用户对某消息是否有权限
     *
     * @return 返回消息
     */
    private ChatMessageDo checkPermission(String username, Integer messageId) {
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
    public void deleteMessage(String username, Integer messageId) {
        checkPermission(username, messageId);
        // 删除消息
        chatMessageMapper.deleteById(messageId);
    }

    /** 编辑已有消息（目前只支持编辑文本消息） */
    @Transactional
    public void editMessage(String username, Integer messageId, String newContent) {
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
    public void readMessage(String username, Integer messageId) {
        ChatMessageDo message = checkPermission(username, messageId);
        ChatFriendDo chatFriend = chatFriendMapper.selectById(username, message.getSource().equals(username) ? message.getTarget() : message.getSource());
        // 判断该消息是否比上次已读消息新
        if (messageId <= chatFriend.getReadChatId()) {
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
        // 得到满足条件的所有消息
        List<String> messages = chatMessageMapper.selectBeforeTimeWithTypes(time, List.of(ChatMsgTypeEnum.TEXT, ChatMsgTypeEnum.IMAGE, ChatMsgTypeEnum.VIDEO, ChatMsgTypeEnum.FILE));
        // 删除消息对应的文件
        for (String message : messages) {
            String path = userUploadPath + message.split(" ", 2)[0];
            File file = new File(path);
            // 删除文件
            if (file.exists()) {
                file.delete();
            }
        }
        // 删除消息
        chatMessageMapper.deleteBeforeTime(time);
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
    public List<ChatMessageDo> loadMessageBefore(String username, String friend, Integer messageId, Integer count) {
        return chatMessageMapper.selectBeforeId(username, friend, messageId, count);
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
    public List<ChatMessageDo> loadMessageBeforeWithKeyword(String username, String friend, Integer messageId, Integer count, String keyword) {
        return chatMessageMapper.selectBeforeIdWithKeyword(username, friend, messageId, count, keyword, ChatMsgTypeEnum.TEXT);
    }

    /**
     * 发送消息
     *
     * @param source  源用户id
     * @param target  目标用户id
     * @param type    消息类型
     * @param content 消息内容（文本消息和文件消息才有，对于文件消息，内容是原文件名）
     * @param file    文件（只有图片、视频、文件消息才有）
     */
    @Transactional
    public void sendMessage(String source, String target, ChatMsgTypeEnum type, String content, MultipartFile file) {
        // 如果内容为空，则抛出异常
        if ((content == null || content.isEmpty()) && (file == null || file.isEmpty())) {
            throw new BusinessException(StatusCodeEnum.MESSAGE_CONTENT_EMPTY);
        }
        // 判断源用户和目标用户是否存在
        if (userMapper.selectById(source) == null || userMapper.selectById(target) == null) {
            throw new BusinessException(StatusCodeEnum.USER_NOT_EXIST);
        }
        // 判断是否被对方拉黑
        ChatFriendDo chatFriend = chatFriendMapper.selectById(target, source);
        if (chatFriend != null && chatFriend.getBan()) {
            throw new BusinessException(StatusCodeEnum.USER_BEEN_BANNED);
        }
        ChatMessageDo chatMessage = new ChatMessageDo();
        // 判断是否是文本消息
        if (type.equals(ChatMsgTypeEnum.TEXT)) {
            // 如果是文本消息，则直接保存消息
            chatMessage.setSource(source);
            chatMessage.setTarget(target);
            chatMessage.setType(type);
            chatMessage.setMessage(content);
            chatMessage.setTime(new Date());
            chatMessageMapper.insert(chatMessage);
        } else {
            // 如果是图片、视频、文件消息，则保存文件
            if (file.isEmpty()) {
                throw new BusinessException(StatusCodeEnum.MESSAGE_CONTENT_EMPTY);
            }
            try {
                // 保存文件到服务器或存储系统中
                String name = SecureNameGenerator.generate();
                String path = userUploadPath + name;
                file.transferTo(new File(path));
                // 保存消息
                chatMessage.setSource(source);
                chatMessage.setTarget(target);
                chatMessage.setType(type);
                // 如果是文件，那么消息内容是服务器文件名<space>原文件名，如果是图片/视频，那么消息内容就是服务器上的文件名
                if (type.equals(ChatMsgTypeEnum.FILE)) {
                    chatMessage.setMessage(name + " " + content);
                } else {
                    chatMessage.setMessage(name);
                }
                chatMessage.setTime(new Date());
                // 插入消息时会得到消息的id
                chatMessageMapper.insert(chatMessage);
            } catch (Exception e) {
                log.info("保存文件失败", e);
                throw new BusinessException(StatusCodeEnum.SERVER_ERROR);
            }
        }
        // 如果之前不存在好友关系，则创建好友关系
        if (chatFriend == null) {
            chatFriend = new ChatFriendDo();
            chatFriend.setUser(target);
            chatFriend.setFriend(source);
            chatFriend.setBan(false);
            chatFriend.setLastChatId(chatMessage.getId());
            chatFriend.setReadChatId(null);
            chatFriend.setUnread(1);
            chatFriendMapper.insert(chatFriend);
        } else {
            // 如果之前存在好友关系，则更新好友关系
            chatFriend.setLastChatId(chatMessage.getId());
            chatFriend.setUnread(chatFriend.getUnread() + 1);
            chatFriendMapper.update(chatFriend);
        }
        ChatFriendDo chatFriend2 = chatFriendMapper.selectById(source, target);
        if (chatFriend2 == null) {
            chatFriend2 = new ChatFriendDo();
            chatFriend2.setUser(source);
            chatFriend2.setFriend(target);
            chatFriend2.setBan(false);
            chatFriend2.setLastChatId(chatMessage.getId());
            chatFriend2.setReadChatId(null);
            chatFriend2.setUnread(0);
            chatFriendMapper.insert(chatFriend2);
        } else {
            chatFriend2.setLastChatId(chatMessage.getId());
            chatFriendMapper.update(chatFriend2);
        }
        // 使用websocket消息通知目标用户
        WSMessageSender.addResponse(new WSResponseVo(WSResponseEnum.CHAT_MESSAGE, new ChatMessageVo(
                chatMessage.getId(),
                chatMessage.getSource(),
                chatMessage.getTarget(),
                chatMessage.getType(),
                chatMessage.getMessage(),
                chatMessage.getTime()
        )));
    }
}
