package com.shuidun.sandbox_town_backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface UserRoleMapper {

    @Select("select role from user_role where username=#{username}")
    Set<String> selectByUserName(String username);

    @Insert("insert into user_role values (#{username}, #{role})")
    void insert(String username, String role);

    @Delete("delete from user_role where username= #{username}")
    void deleteByUsername(String username);
}
