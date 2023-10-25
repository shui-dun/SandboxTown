package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import com.shuidun.sandbox_town_backend.service.ChatFriendService;
import com.shuidun.sandbox_town_backend.service.ChatMessageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private ChatMessageService chatMessageService;

    private ChatFriendService chatFriendService;

    public ChatController(ChatMessageService chatMessageService, ChatFriendService chatFriendService) {
        this.chatMessageService = chatMessageService;
        this.chatFriendService = chatFriendService;
    }

    /** 拉黑用户 */
    @PostMapping("/ban")
    public void banUser(String userId) {
        chatFriendService.banUser(StpUtil.getLoginIdAsString(), userId);
    }

    /** 解除拉黑 */
    @PostMapping("/unban")
    public void unbanUser(String userId) {
        chatFriendService.unbanUser(StpUtil.getLoginIdAsString(), userId);
    }

    /** 删除消息 */
    @PostMapping("/delete")
    public void deleteMessage(String messageId) {
        chatMessageService.deleteMessage(StpUtil.getLoginIdAsString(), messageId);
    }

    /** 编辑消息 */
    @PostMapping("/edit")
    public void editMessage(String messageId, String content) {
        chatMessageService.editMessage(StpUtil.getLoginIdAsString(), messageId, content);
    }

    /** 已读消息 */
    @PostMapping("/read")
    public void readMessage(String messageId) {
        chatMessageService.readMessage(StpUtil.getLoginIdAsString(), messageId);
    }

    /** 加载与某用户在某个消息前的指定长度的消息列表 */
    @PostMapping("/load")
    public void loadMessage(String userId, String messageId, Integer length) {
        chatMessageService.loadMessageBefore(StpUtil.getLoginIdAsString(), userId, messageId, length);
    }

    /** 加载与某用户在某个消息前的、包含某个关键字的、指定长度的文本消息列表 */
    @PostMapping("/search")
    public void searchMessage(String userId, String messageId, Integer length, String keyword) {
        chatMessageService.loadMessageBeforeWithKeyword(StpUtil.getLoginIdAsString(), userId, messageId, length, keyword);
    }

    /**
     * 发送消息
     *
     * @param userId  目标用户id
     * @param type    消息类型
     * @param content 消息内容（只有文本消息才有）
     * @param file    文件（只有图片、视频、文件消息才有）
     */
    @PostMapping("/send")
    public void uploadFile(String userId, ChatMsgTypeEnum type, String content, MultipartFile file) {
        // TODO: 完成发送消息的逻辑
        // if (file.isEmpty()) {
        //     return "File is empty!";
        // }
        //
        // try {
        //     // 保存文件到服务器或存储系统中
        //     byte[] bytes = file.getBytes();
        //     // 使用websocket消息通知目标用户
        // } catch (Exception e) {
        //     return "Error during file upload: " + e.getMessage();
        // }
    }

}
