package com.shuidun.sandbox_town_backend.service;


import com.shuidun.sandbox_town_backend.mapper.RoleMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RoleService {

    private final RoleMapper roleMapper;

    public RoleService(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public Set<String> getRolesByUserName(String username) {
        return roleMapper.getRolesByUserName(username);
    }
}
