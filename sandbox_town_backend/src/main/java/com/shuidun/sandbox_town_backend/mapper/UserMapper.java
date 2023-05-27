package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.User;
import org.apache.ibatis.annotations.*;

import java.util.Set;

@Mapper
public interface UserMapper {
    @Select("select * from user where username=#{username}")
    public User findUserByName(String username);

    @Insert("insert into user values(#{username}, #{password}, #{salt}, #{banEndTime}, #{cheatCount})")
    void insertUser(User user);

    @Update("update user set password=#{password}, salt=#{salt}, banEndTime=#{banEndTime}, cheatCount=#{cheatCount} where username=#{username}")
    void updateUser(User user);

    @Delete("delete from user where username=#{username}")
    int deleteUser(String username);

    @Select("select * from user")
    Set<User> listAll();
}
