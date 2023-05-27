package com.shuidun.sandbox_town_backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface RoleMapper {

    @Select("select role from user_role where username=#{username}")
    Set<String> getRolesByUserName(String username);

    @Insert("insert into user_role values (#{username}, #{role})")
    void insertUserRole(String username, String role);

    @Delete("delete from user_role where username= #{username}")
    void deleteByUsername(String username);
}
