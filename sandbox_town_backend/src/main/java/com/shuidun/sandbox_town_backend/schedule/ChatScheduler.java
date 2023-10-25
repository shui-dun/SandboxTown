package com.shuidun.sandbox_town_backend.schedule;

import com.shuidun.sandbox_town_backend.mixin.Constants;
import com.shuidun.sandbox_town_backend.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class ChatScheduler {
    /** 一天的毫秒数 */
    private static final long ONE_DAY = 86400000;

    /** 聊天保存天数 */
    private static final int CHAT_MESSAGE_EXPIRE_TIME = 30;

    private final ChatMessageService chatMessageService;

    public ChatScheduler(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    /** 每天清理一次聊天记录 */
    @Scheduled(initialDelay = ONE_DAY, fixedDelay = ONE_DAY)
    public void cleanChat() {
        log.info("clean chat");
        // TODO: 应该还要删除聊天记录中的图片和视频和文件
        chatMessageService.deleteMessageBefore(new Date(System.currentTimeMillis() - CHAT_MESSAGE_EXPIRE_TIME * ONE_DAY));
    }
}
