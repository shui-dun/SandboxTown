package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.ChatFriendService;
import com.shuidun.sandbox_town_backend.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatMessageService chatMessageService;

    private final ChatFriendService chatFriendService;

    public ChatController(ChatMessageService chatMessageService, ChatFriendService chatFriendService) {
        this.chatMessageService = chatMessageService;
        this.chatFriendService = chatFriendService;
    }

    /** 拉黑用户 */
    @PostMapping("/ban")
    public RestResponseVo<?> banUser(String userId) {
        chatFriendService.banUser(StpUtil.getLoginIdAsString(), userId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
    }

    /** 解除拉黑 */
    @PostMapping("/unban")
    public RestResponseVo<?> unbanUser(String userId) {
        chatFriendService.unbanUser(StpUtil.getLoginIdAsString(), userId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
    }

    /** 删除消息 */
    @PostMapping("/delete")
    public RestResponseVo<?> deleteMessage(Integer messageId) {
        chatMessageService.deleteMessage(StpUtil.getLoginIdAsString(), messageId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
    }

    /** 编辑消息 */
    @PostMapping("/edit")
    public RestResponseVo<?> editMessage(Integer messageId, String content) {
        chatMessageService.editMessage(StpUtil.getLoginIdAsString(), messageId, content);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
    }

    /** 已读消息 */
    @PostMapping("/read")
    public RestResponseVo<?> readMessage(Integer messageId) {
        chatMessageService.readMessage(StpUtil.getLoginIdAsString(), messageId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
    }

    /** 加载与某用户在某个消息前（包含该消息本身）的指定长度的消息列表 */
    @GetMapping("/loadBefore")
    public RestResponseVo<?> loadMessageBefore(String userId, Integer messageId, Integer length) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, chatMessageService.loadMessageBefore(StpUtil.getLoginIdAsString(), userId, messageId, length));
    }

    /** 加载与某用户在某个消息后（不包含该消息本身）的指定长度的消息列表 */
    @GetMapping("/loadAfter")
    public RestResponseVo<?> loadMessageAfter(String userId, Integer messageId, Integer length) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, chatMessageService.loadMessageAfter(StpUtil.getLoginIdAsString(), userId, messageId, length));
    }

    /** 加载与某用户在某个消息前的、包含某个关键字的、指定长度的文本消息列表 */
    @GetMapping("/search")
    public RestResponseVo<?> searchMessage(String userId, Integer messageId, Integer length, String keyword) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, chatMessageService.loadMessageBeforeWithKeyword(StpUtil.getLoginIdAsString(), userId, messageId, length, keyword));
    }

    /**
     * 发送消息
     *
     * @param userId  目标用户id
     * @param type    消息类型
     * @param content 消息内容（文本消息和文件消息才有，对于文件消息，内容是原文件名）
     * @param file    文件（只有图片、视频、文件消息才有）
     */
    @PostMapping("/send")
    public RestResponseVo<?> uploadFile(String userId, ChatMsgTypeEnum type, String content, MultipartFile file) {
        log.info("userId: {}, type: {}, content: {}, file: {}", userId, type, content, file);
        chatMessageService.sendMessage(StpUtil.getLoginIdAsString(), userId, type, content, file);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS, null);
    }

}
