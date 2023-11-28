package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.ChatMessageDo;
import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageDo> {

    /** 查询某个用户和某个好友的新于某个id的消息数量 */
    default long countNewerThanId(String user, String friend, Integer id) {
        return selectCount(new LambdaQueryWrapper<ChatMessageDo>()
                .eq(ChatMessageDo::getSource, user)
                .eq(ChatMessageDo::getTarget, friend)
                .gt(ChatMessageDo::getId, id));
    }

    /** 获得指定时间前的、类型属于指定类型的所有消息的消息内容 */
    default List<String> selectBeforeTimeWithTypes(Date time, List<ChatMsgTypeEnum> types) {
        return selectList(new LambdaQueryWrapper<ChatMessageDo>()
                .select(ChatMessageDo::getMessage)
                .lt(ChatMessageDo::getTime, time)
                .in(ChatMessageDo::getType, types))
                .stream()
                .map(ChatMessageDo::getMessage)
                .toList();
    }


    /** 删除指定时间前的所有消息 */
    default void deleteBeforeTime(Date time) {
        delete(new LambdaQueryWrapper<ChatMessageDo>()
                .lt(ChatMessageDo::getTime, time));
    }

    /** 加载两用户在某个消息前的指定长度的消息列表（包含该消息本身） */
    default List<ChatMessageDo> selectBeforeId(String username, String friend, Integer messageId, Integer count) {
        return selectList(new LambdaQueryWrapper<ChatMessageDo>()
                .nested(i -> i.eq(ChatMessageDo::getSource, username).eq(ChatMessageDo::getTarget, friend)
                        .or().eq(ChatMessageDo::getSource, friend).eq(ChatMessageDo::getTarget, username))
                .le(ChatMessageDo::getId, messageId)
                .orderByAsc(ChatMessageDo::getId)
                .last("limit " + count));
    }


    /** 加载两用户在某个消息后的指定长度的消息列表 （但不包含某消息本身） */
    default List<ChatMessageDo> selectAfterId(String username, String friend, Integer messageId, Integer count) {
        return selectList(new LambdaQueryWrapper<ChatMessageDo>()
                .nested(i -> i.eq(ChatMessageDo::getSource, username).eq(ChatMessageDo::getTarget, friend)
                        .or().eq(ChatMessageDo::getSource, friend).eq(ChatMessageDo::getTarget, username))
                .gt(ChatMessageDo::getId, messageId)
                .orderByAsc(ChatMessageDo::getId)
                .last("limit " + count));
    }

    /** 加载两用户在某个消息前的（包含该消息本身）、包含某个关键字的、指定长度的、指定类型的消息列表 **/
    default List<ChatMessageDo> selectBeforeIdWithKeyword(String username, String friend, Integer messageId, Integer count, String keyword, ChatMsgTypeEnum type) {
        return selectList(new LambdaQueryWrapper<ChatMessageDo>()
                .nested(i -> i.eq(ChatMessageDo::getSource, username).eq(ChatMessageDo::getTarget, friend)
                        .or().eq(ChatMessageDo::getSource, friend).eq(ChatMessageDo::getTarget, username))
                .le(ChatMessageDo::getId, messageId)
                .like(ChatMessageDo::getMessage, keyword)
                .eq(ChatMessageDo::getType, type)
                .orderByAsc(ChatMessageDo::getId)
                .last("limit " + count));
    }
}
