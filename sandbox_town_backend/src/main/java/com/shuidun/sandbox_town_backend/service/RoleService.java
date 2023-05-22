package com.shuidun.sandbox_town_backend.service;

import org.springframework.stereotype.Service;

import java.util.Set;

public interface RoleService {
    public Set<String> getRolesByUserName(String name);
}
