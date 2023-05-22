package com.shuidun.sandbox_town_backend.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String name;
    private String passwd;

    // 密码的盐
    private String salt;

    // 用户所有角色值，用于shiro做角色权限的判断
    private Set<String> roles = new HashSet<>();

    // 用户所有权限值，用于shiro做资源权限的判断
    private Set<String> perms = new HashSet<>();

    public User(String name, String passwd, String salt) {
        this.name = name;
        this.passwd = passwd;
        this.salt = salt;
    }
}
