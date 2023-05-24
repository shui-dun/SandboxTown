package com.shuidun.sandbox_town_backend.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpImpl implements StpInterface {
    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 通过传递过来的 loginId 去数据库查询该用户所拥有的权限 这里做演示 就不查询数据库了
        List<String> list = new ArrayList<>();
        list.add("user:list");
        list.add("user:insert");
        list.add("user:update");
        list.add("user:delete");
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 通过传递过来的 loginId 去数据库查询该用户所拥有的权限 这里做演示 就不查询数据库了
        List<String> list = new ArrayList<>();
        list.add("admin");
        list.add("super-admin");
        return list;
    }

}