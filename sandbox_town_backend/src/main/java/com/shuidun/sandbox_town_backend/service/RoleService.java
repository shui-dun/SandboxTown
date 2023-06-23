package com.shuidun.sandbox_town_backend.service;


import com.shuidun.sandbox_town_backend.mapper.UserRoleMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RoleService {

    private final UserRoleMapper userRoleMapper;

    public RoleService(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    public Set<String> getRolesByUserName(String username) {
        return userRoleMapper.selectByUserName(username);
    }
}
