package com.shuidun.sandbox_town_backend.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface RoleMapper {

    @Select("select role from user_role where user=#{name}")
    Set<String> getRolesByUserName(String name);

    @Insert("insert into user_role values (#{user}, #{role})")
    void insertUserRole(String user, String role);

    @Delete("delete from user_role where user= #{name}")
    void deleteByUsername(String name);
}
