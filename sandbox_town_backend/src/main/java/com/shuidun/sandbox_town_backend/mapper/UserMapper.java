package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface UserMapper {
    @Select("select * from user where username=#{username}")
    public User findUserByName(String username);

    @Insert("insert into user (username, password, salt) values(#{username}, #{password}, #{salt})")
    void insertUser(User user);

    @Delete("delete from user where username=#{username}")
    int deleteUser(String username);

    @Select("select * from user")
    Set<User> listAll();
}
