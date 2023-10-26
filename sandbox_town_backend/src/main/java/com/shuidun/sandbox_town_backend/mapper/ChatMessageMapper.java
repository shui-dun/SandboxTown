package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.ChatMessageDo;
import com.shuidun.sandbox_town_backend.enumeration.ChatMsgTypeEnum;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageDo> {

    /** 查询某个用户和某个好友的新于某个id的消息数量 */
    @Select("select count(*) from chat_message where source = #{user} and target = #{friend} and id > #{id}")
    Integer countNewerThanId(String user, String friend, Integer id);

    /** 获得指定时间前的、类型属于指定类型的所有消息的消息内容 */
    @Select("""
            <script>
                select message from chat_message where time &lt; #{time} and type in
                <foreach collection='types' item='type' open='(' separator=',' close=')'>
                    #{type}
                </foreach>
            </script>
            """)
    List<String> selectBeforeTimeWithTypes(Date time, List<ChatMsgTypeEnum> types);

    /** 删除指定时间前的所有消息 */
    @Delete("delete from chat_message where time < #{time}")
    void deleteBeforeTime(Date time);

    /** 加载两用户在某个消息前的指定长度的消息列表（包含该消息本身） */
    @Select("select * from chat_message where ((source = #{username} and target = #{friend}) or (source = #{friend} and target = #{username})) and id <= #{messageId} order by id limit #{count}")
    List<ChatMessageDo> selectBeforeId(String username, String friend, Integer messageId, Integer count);

    /** 加载两用户在某个消息后的指定长度的消息列表 （但不包含某消息本身） */
    @Select("select * from chat_message where ((source = #{username} and target = #{friend}) or (source = #{friend} and target = #{username})) and id > #{messageId} order by id limit #{count}")
    List<ChatMessageDo> selectAfterId(String username, String friend, Integer messageId, Integer count);

    /** 加载两用户在某个消息前的（包含该消息本身）、包含某个关键字的、指定长度的、指定类型的消息列表 **/
    @Select("""
            select * from chat_message
            where ((source = #{username} and target = #{friend}) or (source = #{friend} and target = #{username}))
            and id <= #{messageId} and message like concat('%', #{keyword}, '%') and type = #{type}
            order by id limit #{count}
            """)
    List<ChatMessageDo> selectBeforeIdWithKeyword(String username, String friend, Integer messageId, Integer count, String keyword, ChatMsgTypeEnum type);
}
