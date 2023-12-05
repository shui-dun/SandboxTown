package com.shuidun.sandbox_town_backend.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.shuidun.sandbox_town_backend.bean.ChatMessageDo;
import com.shuidun.sandbox_town_backend.bean.RestResponseVo;
import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import com.shuidun.sandbox_town_backend.enumeration.StatusCodeEnum;
import com.shuidun.sandbox_town_backend.service.ChatFriendService;
import com.shuidun.sandbox_town_backend.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
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

    @Operation(summary = "拉黑用户")
    @PostMapping("/ban")
    public RestResponseVo<Void> banUser(@NotNull @RequestParam String userId) {
        chatFriendService.banUser(StpUtil.getLoginIdAsString(), userId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @Operation(summary = "解除拉黑")
    @PostMapping("/unban")
    public RestResponseVo<Void> unbanUser(@NotNull @RequestParam String userId) {
        chatFriendService.unbanUser(StpUtil.getLoginIdAsString(), userId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @Operation(summary = "删除消息")
    @PostMapping("/delete")
    public RestResponseVo<Void> deleteMessage(@NotNull @RequestParam Integer messageId) {
        chatMessageService.deleteMessage(StpUtil.getLoginIdAsString(), messageId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @Operation(summary = "编辑消息")
    @PostMapping("/edit")
    public RestResponseVo<Void> editMessage(@NotNull @RequestParam Integer messageId, @NotNull @RequestParam String content) {
        chatMessageService.editMessage(StpUtil.getLoginIdAsString(), messageId, content);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @Operation(summary = "已读消息")
    @PostMapping("/read")
    public RestResponseVo<Void> readMessage(@NotNull @RequestParam Integer messageId) {
        chatMessageService.readMessage(StpUtil.getLoginIdAsString(), messageId);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

    @Operation(summary = "加载与某用户在某个消息前（包含该消息本身）的指定长度的消息列表")
    @GetMapping("/loadBefore")
    public RestResponseVo<List<ChatMessageDo>> loadMessageBefore(@NotNull @RequestParam String userId, @NotNull @RequestParam Integer messageId, @NotNull @RequestParam Integer length) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                chatMessageService.loadMessageBefore(StpUtil.getLoginIdAsString(), userId, messageId, length));
    }

    @Operation(summary = "加载与某用户在某个消息后（不包含该消息本身）的指定长度的消息列表")
    @GetMapping("/loadAfter")
    public RestResponseVo<List<ChatMessageDo>> loadMessageAfter(@NotNull @RequestParam String userId, @NotNull @RequestParam Integer messageId, @NotNull @RequestParam Integer length) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                chatMessageService.loadMessageAfter(StpUtil.getLoginIdAsString(), userId, messageId, length));
    }

    @Operation(summary = "加载与某用户在某个消息前的、包含某个关键字的、指定长度的文本消息列表")
    @GetMapping("/search")
    public RestResponseVo<List<ChatMessageDo>> searchMessage(@NotNull @RequestParam String userId, @NotNull @RequestParam Integer messageId, @NotNull @RequestParam Integer length, @NotNull @RequestParam String keyword) {
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS,
                chatMessageService.loadMessageBeforeWithKeyword(StpUtil.getLoginIdAsString(), userId, messageId, length, keyword));
    }

    @Operation(summary = "发送消息")
    @PostMapping(value = "/send")
    public RestResponseVo<Void> send(@NotNull @RequestParam @Parameter(description = "目标用户id") String userId,
                                     @NotNull @RequestParam @Parameter(description = "消息类型") ChatMsgTypeEnum type,
                                     @NotNull @RequestParam @Parameter(description = "消息内容（文本消息和文件消息才有，对于文件消息，内容是原文件名）") String content,
                                     @Nullable @RequestParam @Parameter(description = "文件（只有图片、视频、文件消息才有）") MultipartFile file) {
        chatMessageService.sendMessage(StpUtil.getLoginIdAsString(), userId, type, content, file);
        return new RestResponseVo<>(StatusCodeEnum.SUCCESS);
    }

}
