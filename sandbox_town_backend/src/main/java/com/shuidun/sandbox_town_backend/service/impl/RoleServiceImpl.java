package com.shuidun.sandbox_town_backend.service.impl;


import com.shuidun.sandbox_town_backend.mapper.RoleMapper;
import com.shuidun.sandbox_town_backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public Set<String> getRolesByUserName(String username) {
        return roleMapper.getRolesByUserName(username);
    }
}
