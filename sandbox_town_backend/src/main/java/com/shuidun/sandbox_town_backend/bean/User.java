package com.shuidun.sandbox_town_backend.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String username;
    private String password;

    // 密码的盐
    private String salt;

    // 用户所有角色值，用于权限的判断
    private Set<String> roles = new HashSet<>();

    private Timestamp banEndTime;

    private int cheatCount;

    public User(String username, String password, String salt) {
        this.username = username;
        this.password = password;
        this.salt = salt;
    }
}
