package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.UserDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<UserDo> {
    /** 根据用户名搜索用户列表 */
    @Select("select * from user where username like concat('%',#{username},'%')")
    List<UserDo> searchUser(String username);
}
