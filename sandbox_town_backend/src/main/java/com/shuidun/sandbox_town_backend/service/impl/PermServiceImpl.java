package com.shuidun.sandbox_town_backend.service.impl;

import com.shuidun.sandbox_town_backend.mapper.PermMapper;
import com.shuidun.sandbox_town_backend.service.PermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class PermServiceImpl implements PermService {

    private final PermMapper permMapper;

    public PermServiceImpl(PermMapper permMapper) {
        this.permMapper = permMapper;
    }

    @Override
    public Set<String> getPermsByRoles(Set<String> roles) {
        Set<String> ans = new HashSet<>();
        for (String role : roles) {
            ans.addAll(permMapper.getPermsByRole(role));
        }
        return ans;
    }
}
