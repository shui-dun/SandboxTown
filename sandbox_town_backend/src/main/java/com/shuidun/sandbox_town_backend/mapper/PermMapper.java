package com.shuidun.sandbox_town_backend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface PermMapper {

    @Select("select perm from role_perm where role = #{role}")
    Set<String> getPermsByRole(String role);
}
