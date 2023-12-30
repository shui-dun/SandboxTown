package com.shuidun.sandbox_town_backend.config;

import cn.dev33.satoken.stp.StpInterface;
import com.shuidun.sandbox_town_backend.service.RoleService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class StpImpl implements StpInterface {
    private final RoleService roleService;

    public StpImpl(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Set<String> roles = roleService.getRolesByUserName(loginId.toString());
        return new ArrayList<>(roles);
    }

}