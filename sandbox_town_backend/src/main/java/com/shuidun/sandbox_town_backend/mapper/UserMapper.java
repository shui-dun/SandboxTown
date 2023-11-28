package com.shuidun.sandbox_town_backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shuidun.sandbox_town_backend.bean.UserDo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<UserDo> {
    /** 根据用户名搜索用户列表 */
    default List<UserDo> searchUser(String username) {
        return selectList(new LambdaQueryWrapper<UserDo>()
                .like(UserDo::getUsername, username));
    }
}
