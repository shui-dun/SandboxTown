package com.shuidun.sandbox_town_backend.mapper;

import com.shuidun.sandbox_town_backend.bean.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface UserMapper {
    @Select("select * from user where name=#{username}")
    public User findUserByName(String username);

    @Insert("insert into user values(#{name}, #{passwd}, #{salt})")
    void insertUser(User user);

    @Delete("delete from user where name=#{name}")
    int deleteUser(String name);

    @Select("select * from user")
    Set<User> listAll();
}
