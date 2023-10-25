package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.ChatFriendDo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ChatFriendMapper {
    /** 根据用户名和好友名查询好友关系 */
    @Select("select * from chat_friend where user = #{user} and friend = #{friend}")
    ChatFriendDo selectById(String user, String friend);

    /** 更新好友关系 */
    @Update("update chat_friend set ban = #{ban}, last_chat_id = #{lastChatId}, read_chat_id = #{readChatId}, unread = #{unread} where user = #{user} and friend = #{friend}")
    void update(ChatFriendDo chatFriend);

    /** 创建好友关系 */
    @Insert("insert into chat_friend (user, friend, ban, last_chat_id, read_chat_id, unread) values (#{user}, #{friend}, #{ban}, #{lastChatId}, #{readChatId}, #{unread})")
    void insert(ChatFriendDo chatFriend);
}
